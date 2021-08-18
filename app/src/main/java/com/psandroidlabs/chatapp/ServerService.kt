package com.psandroidlabs.chatapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random

class ServerService : Service(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private val mutex = Mutex()
    private val listMutex = Mutex()

    private lateinit var serverSocket: ServerSocket
    private val userList: ArrayList<User> = arrayListOf()

    private lateinit var notificationManager: ChatNotificationManager
    private var password: String? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_STOP) {
                onDestroy()
            }
        }
    }

    override fun onCreate() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Constants.ACTION_STOP))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = ChatNotificationManager(applicationContext, Constants.FOREGROUND_CHAT_CHANNEL)
        startForeground(100, notificationManager.foregroundNotification())

        password = intent.getStringExtra(Constants.PASSWORD)

        startServer()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        closeServer()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        this.cancel()
        stopForeground(true)

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startServer() {
        var count = 0
        launch(Dispatchers.IO) {
            serverSocket = ServerSocket(Constants.CHAT_DEFAULT_PORT)

            while (isActive && count <= 5) {
                try {
                    val socket = serverSocket.accept()
                    val user = User(socket, Profile())

                    userList.add(user)

                    socketListen(user)

                    count++
                } catch (e: java.net.SocketException) {
                    return@launch
                }
            }
        }
    }

    private fun socketListen(user: User) {
        launch(Dispatchers.IO) {
            val scanner = Scanner(user.socket.getInputStream())

            while (isActive) {
                if (scanner.hasNextLine()) {
                    val json = scanner.nextLine()

                    val message = ChatManager.serializeMessage(json)

                    if (message?.type != MessageType.JOIN.code) {
                        if (message?.id != user.profile.id) {
                            removeSocket(user)
                        } else {
                            forwardMessage(user.socket, json.toByteArray(Charsets.UTF_8))
                        }
                    } else {
                        authenticate(message, user)
                        forwardMessage(user.socket, json.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    }

    @Synchronized
    private fun authenticate(message: Message, user: User) {
        launch(Dispatchers.IO) {
            mutex.withLock {
                if (password != null) {
                    if (message.join?.password == password && !message.username.isNullOrBlank()) {
                        accept(message, user)
                    } else {
                        refuse(user)
                    }
                } else {
                    accept(message, user)
                }
            }
        }
    }

    private fun accept(joinMessage: Message, user: User) {
        val id = generateCode()

        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.ACCEPTED, id, getProfileList()
            )
        ).toByteArray(Charsets.UTF_8)

        user.profile.let {
            it.id = id
            it.photoProfile = joinMessage.base64Data
            it.name = joinMessage.username ?: codeGenerator().toString()
            it.scoreTicTacToe = 0
        }

        user.socket.getOutputStream().write(json)

        forwardMessage(user.socket, ChatManager.parseToJson(joinMessage).toByteArray(Charsets.UTF_8))
    }

    private fun refuse(user: User) {
        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.WRONG_PASSWORD, AcceptedStatus.WRONG_PASSWORD.code
            )
        ).toByteArray(Charsets.UTF_8)

        user.socket.getOutputStream().write(json)

        removeSocket(user)
    }

    @Synchronized
    private fun forwardMessage(socket: Socket, message: ByteArray) {
        launch(Dispatchers.IO) {
            listMutex.withLock {
                userList.forEach { user ->
                    if (user.socket.inetAddress != socket.inetAddress) {
                        try {
                            user.socket.getOutputStream()?.write(message)
                        } catch (e: java.net.SocketException) {
                            removeSocket(user)
                        }
                    }
                }
            }
        }
    }

    private fun generateCode(): Int {
        var id = codeGenerator()

        runBlocking {
            launch(Dispatchers.IO) {
                listMutex.withLock {
                    userList.forEach { user ->
                        if (id == user.profile?.id) {
                            id = codeGenerator()
                        }
                    }
                }
            }
        }

        return id
    }

    private fun codeGenerator(): Int {
        return List(4) { Random.nextInt(0, 100) }.joinToString("").toInt()
    }

    private fun getProfileList(): String {
        val profileList: ArrayList<Profile> = arrayListOf()

        launch(Dispatchers.IO) {
            listMutex.withLock {
                userList.forEach { user ->
                    user.profile?.let { profileList.add(it) }
                }
            }
        }

        return ChatManager.parseProfileList(profileList)
    }

    @Synchronized
    private fun removeSocket(user: User) {
        launch(Dispatchers.IO) {
            listMutex.withLock {
                userList.remove(user)
            }
        }
    }

    private fun closeServer() {
        serverSocket.close()
        userList.removeAll(userList)
    }
}