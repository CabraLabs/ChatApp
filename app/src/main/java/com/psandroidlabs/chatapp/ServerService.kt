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
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.random.Random

class ServerService : Service(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var serverSocket: ServerSocket
    private var socketList: ArrayList<Socket?> = arrayListOf()

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

            while (count <= 3 && isActive) {
                try {
                    val socket = serverSocket.accept()
                    socketList.add(socket)
                    socketListen(socket)
                    count++
                } catch (e: java.net.SocketException) {
                    return@launch
                }
            }
        }
    }

    private fun socketListen(socket: Socket) {
        launch(Dispatchers.IO) {
            val scanner = Scanner(socket.getInputStream())

            while (isActive) {
                if (scanner.hasNextLine()) {
                    val json = scanner.nextLine()

                    val message = ChatManager.serializeMessage(json)

                    if (message?.type != MessageType.JOIN.code) {
                        forwardMessage(socket, json.toByteArray(Charsets.UTF_8))
                    } else {
                        authenticate(message, socket)
                        forwardMessage(socket, json.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    }

    private fun authenticate(message: Message, socket: Socket) {
        runBlocking {
            launch(Dispatchers.IO) {
                if (password != null) {
                    if (message.join?.password == password) {
                        accept(socket)
                    } else {
                        refuse(socket)
                    }
                } else {
                    accept(socket)
                }
            }
        }
    }

    private fun accept(socket: Socket) {
        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.ACCEPTED, generateCode()
            )
        ).toByteArray(Charsets.UTF_8)

        socket.getOutputStream().write(json)

        forwardMessage(socket, json)
    }

    private fun refuse(socket: Socket) {
        val json = ChatManager.parseToJson(
            ChatManager.parseAcceptMessage(
                AcceptedStatus.WRONG_PASSWORD, AcceptedStatus.WRONG_PASSWORD.code
            )
        ).toByteArray(Charsets.UTF_8)

        socket.getOutputStream().write(json)
        removeSocket(socket)
    }

    @Synchronized
    private fun forwardMessage(socket: Socket, message: ByteArray) {
        launch(Dispatchers.IO) {
            socketList.forEach { sock ->
                if (sock?.inetAddress != socket.inetAddress ) {
                    try {
                        sock?.getOutputStream()?.write(message)
                    } catch (e: java.net.SocketException) {
                        if (sock != null) {
                            removeSocket(socket)
                        }
                    }
                }
            }
        }
    }

    private fun generateCode(): Int {
        val id = List(4) { Random.nextInt(0, 100) }
        return id.joinToString("").toInt()
    }

    @Synchronized
    private fun removeSocket(socket: Socket) {
        launch(Dispatchers.IO) {
            socketList.remove(socket)
        }
    }

    private fun closeServer() {
        serverSocket.close()
        socketList.removeAll(socketList)
    }
}