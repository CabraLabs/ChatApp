package com.alexparra.chatapp

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.alexparra.chatapp.models.ChatNotificationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList

class ServerService: Service(), CoroutineScope {

    private val STOP = "STOP"

    private var SERVER_START = false
    private var running = true

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var serverSocket: ServerSocket
    private var socketList: ArrayList<Socket?> = arrayListOf()

    private val channel = Channel<Pair<InetAddress, ByteArray>>()

    override fun onCreate() {
        startServer()
        forwardMessage()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationManager = ChatNotificationManager(applicationContext, "foreground")
        startForeground(100, notificationManager.foregroundNotification(""))

        if (intent.getStringExtra("STOP") == STOP) {
            stopForeground(true)
            notificationManager.cancelNotification()
            onDestroy()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (SERVER_START) {
            closeServer()
        }

        channel.close()
        running = false
        this.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startServer() {
        var count = 0
        launch(Dispatchers.IO) {
            serverSocket = ServerSocket(1027)

            while (count <= 3) {
                try {
                    val socket = serverSocket.accept()
                    socketList.add(socket)
                    socketListen(socket)
                    count++
                    SERVER_START = true
                } catch (e: java.net.BindException) {
                    // TODO
                }
            }
        }
    }

    private fun socketListen(socket: Socket) {
        launch(Dispatchers.IO) {
            val scanner = Scanner(socket.getInputStream())

            if (scanner.hasNext()) {
                var message = scanner.nextLine().toByteArray(Charsets.UTF_8)
                channel.send(Pair(socket.localAddress, message))
            }
        }
    }

    private fun forwardMessage() {
        launch(Dispatchers.IO) {
            while (running) {
                val message = channel.receive()
                socketList.forEach { socket ->
                    if (message.first != socket?.localAddress) {
                        try {
                            socket?.getOutputStream()?.write(message.second)
                        } catch (e: java.net.SocketException) {
                            socketList.remove(socket)
                        }
                    }
                }
            }
        }
    }

    private fun closeServer() {
        socketList.forEach { socket ->
            socket?.close()
        }
        serverSocket.close()
    }
}