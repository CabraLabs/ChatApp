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
import java.util.concurrent.locks.Lock
import kotlin.collections.ArrayList

class ServerService: Service(), CoroutineScope {

    private val STOP = "STOP"

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private val serverSocket = ServerSocket(1027)

    private var running = true

    override fun onCreate() {
        startServer()
        forwardMessage()
    }

    private lateinit var socketList: ArrayList<Socket>

    private val channel = Channel<Pair<InetAddress, ByteArray>>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationManager = ChatNotificationManager(applicationContext, "1")
        startForeground(1, notificationManager.foregroundNotification(""))

        if (intent.action == STOP) {
            onDestroy()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
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
            while (count <= 3) {
                try {
                    val socket = serverSocket.accept()
                    socketList.add(socket)
                    socketListen(socket)
                    count++
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
                    if (message.first != socket.localAddress) {
                        try {
                            socket.getOutputStream().write(message.second)
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
            socket.close()
        }
        serverSocket.close()
    }
}