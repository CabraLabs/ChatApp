package com.psandroidlabs.chatapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.utils.Constants
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerService : Service(), CoroutineScope {

    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var serverSocket: ServerSocket
    private var socketList: ArrayList<Socket?> = arrayListOf()

    private lateinit var notificationManager: ChatNotificationManager

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
        // TODO take the server info to be displayed on the foreground notification
        startForeground(100, notificationManager.foregroundNotification(""))
        startServer()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (running) {
            closeServer()
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        this.cancel()
        stopForeground(true)
        notificationManager.cancelNotification()
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startServer() {
        var count = 0
        launch(Dispatchers.IO) {
            serverSocket = ServerSocket(Constants.CHAT_DEFAULT_PORT)

            while (count <= 3) {
                try {
                    val socket = serverSocket.accept()
                    socketList.add(socket)
                    socketListen(socket)
                    count++
                    running = true
                } catch (e: java.net.SocketException) {
                    return@launch
                }
            }
        }
    }

    private fun socketListen(socket: Socket) {
        launch(Dispatchers.IO) {
            val scanner = Scanner(socket.getInputStream())

            while (true) {
                if (scanner.hasNextLine()) {
                    var message = scanner.nextLine()
                    val sendMessage = "$message\n"
                    forwardMessage(socket, sendMessage.toByteArray(Charsets.UTF_8))
                }
            }
        }
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

    @Synchronized
    private fun removeSocket(socket: Socket) {
        launch(Dispatchers.IO) {
            socketList.remove(socket)
        }
    }

    private fun closeServer() {
        socketList.forEach { socket ->
            socket?.close()
        }
        serverSocket.close()
    }
}