package com.psandroidlabs.chatapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.ChatNotificationManager
import com.psandroidlabs.chatapp.utils.Constants
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
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

    private val bytePing = Constants.PING.toByteArray(Charsets.UTF_8)

    private lateinit var serverSocket: ServerSocket
    private val userList: ArrayList<User> = arrayListOf()

    private lateinit var notificationManager: ChatNotificationManager
    private var password: String? = null

    private var startId = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_STOP) {
                closeServer()
            }
        }
    }

    override fun onCreate() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(Constants.ACTION_STOP))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        this.startId = startId

        password = intent.getStringExtra(Constants.PASSWORD)
        val port = intent.getIntExtra(Constants.PORT, Constants.PORT_1027)

        notificationManager =
            ChatNotificationManager(applicationContext, Constants.FOREGROUND_CHAT_CHANNEL)
        startForeground(100, notificationManager.foregroundNotification(port))

        startServer(port)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startServer(port: Int) {
        var count = 0
        launch(Dispatchers.IO) {
            serverSocket = ServerSocket(port)
            running = true
            ping()

            while (isActive && count <= Constants.MAX_SERVER_USERS) {
                try {
                    val socket = serverSocket.accept().apply {
                        sendBufferSize = Constants.SOCKET_BUFFER_SIZE
                        receiveBufferSize = Constants.SOCKET_BUFFER_SIZE
                    }
                    val user = User(socket, Profile())

                    listMutex.withLock {
                        userList.add(user)
                    }

                    socketListen(user)

                    count++
                } catch (e: java.net.SocketException) {
                    return@launch
                }
            }
        }
    }

    private fun ping() {
        launch(Dispatchers.IO) {
            while (isActive) {
                delay(5000)
                forwardMessage(ping = true)
            }
        }
    }

    private fun socketListen(user: User) {
        launch(Dispatchers.IO) {
            val scanner = Scanner(user.socket.getInputStream())

            while (isActive) {
                if (scanner.hasNextLine()) {
                    scanner.reset()
                    val json = scanner.nextLine() + "\n"

                    Log.d("Server received:", json)

                    try {
                        val message = ChatManager.serializeMessage(json)

                        when (message?.type) {
                            MessageType.JOIN.code -> {
                                authenticate(message, user)
                            }

                            MessageType.LEAVE.code -> {
                                removeSocket(user)
                                forwardMessage(socket = user.socket, message = json.toByteArray(Charsets.UTF_8))
                            }

                            MessageType.TIC_INVITE.code -> {
                                TODO()
                            }

                            else -> {
                                if (message?.id != user.profile.id) {
                                    removeSocket(user)
                                } else {
                                    forwardMessage(socket = user.socket, message = json.toByteArray(Charsets.UTF_8))
                                }
                            }
                        }
                    } catch (e: JsonDataException) {
                        Log.e(
                            "Server received error",
                            "Json data error: $json"
                        )
                    } catch (e: JsonEncodingException) {
                        Log.e("Server received error", "Json encoding error: $json")
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

        user.profile.let {
            it.id = id
            it.photoProfile = joinMessage.join?.avatar
            it.name = joinMessage.username ?: codeGenerator().toString()
            it.scoreTicTacToe = 0
        }

        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.ACCEPTED, id, getProfileList()
            )
        ).toByteArray(Charsets.UTF_8)

        forwardMessage(socket = user.socket, message = json, accept = true)
        joinMessage.id = id

        if (user.socket.inetAddress == serverSocket.inetAddress) {
            joinMessage.join?.isAdmin = true
            forwardMessage(
                socket = user.socket,
                message = ChatManager.parseToJson(joinMessage).toByteArray(Charsets.UTF_8),
                accept = false,
            )
        } else {
            forwardMessage(
                socket = user.socket,
                message = ChatManager.parseToJson(joinMessage).toByteArray(Charsets.UTF_8),
                accept = false,
            )
        }
    }

    private fun refuse(user: User) {
        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.WRONG_PASSWORD, AcceptedStatus.WRONG_PASSWORD.code
            )
        ).toByteArray(Charsets.UTF_8)

        forwardMessage(socket = user.socket, message = json, accept = true)
        removeSocket(user)
    }

    private fun forwardMessage(
        socket: Socket? = null,
        message: ByteArray? = null,
        ping: Boolean = false,
        accept: Boolean = false
    ) {
        launch(Dispatchers.IO) {
            listMutex.withLock {
                if (!accept) {
                    userList.forEach { user ->
                        try {
                            if (ping) {
                                user.socket.getOutputStream().write(bytePing)
                                Log.d("Server sent", "PING MESSAGE")
                            } else {
                                if (user.socket.inetAddress != socket?.inetAddress) {
                                    user.socket.getOutputStream()?.write(message)
                                    Log.d("Server sent", "NORMAL MESSAGE")
                                }
                            }
                        } catch (e: java.net.SocketException) {
                            removeSocket(user = user)
                        }
                    }
                } else {
                    socket?.getOutputStream()?.write(message)
                    Log.d("Server sent", "ACCEPTED/REVOKED MESSAGE")
                }
            }
        }
    }

    private fun generateCode(): Int {
        var id = codeGenerator()

        runBlocking {
            launch(Dispatchers.Default) {
                listMutex.withLock {
                    userList.forEach { user ->
                        if (id == user.profile.id) {
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

    private fun getProfileList(): String? {
        val profileList: ArrayList<Profile> = arrayListOf()

        runBlocking {
            launch(Dispatchers.Default) {
                listMutex.withLock {
                    userList.forEach { user ->
                        user.profile.let { profileList.add(it) }
                    }
                }
            }
        }

        if (profileList.isEmpty()) {
            return null
        }

        return ChatManager.parseProfileList(profileList)
    }

    private fun removeSocket(user: User) {
        launch(Dispatchers.IO) {
            listMutex.withLock {
                userList.remove(user)
                Log.d("Server removing", "DISCONNECTING ${user.socket.inetAddress}")
            }
        }
    }

    private fun closeServer() {
        running = false
        stopForeground(true)
        serverSocket.close()

        runBlocking {
            listMutex.withLock {
                userList.forEach { user ->
                    user.socket.close()
                }
            }
        }

        stopSelf(startId)
    }

    companion object {
        var running = false
    }
}