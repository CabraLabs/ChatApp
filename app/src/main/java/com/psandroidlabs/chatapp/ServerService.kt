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
import kotlinx.coroutines.channels.Channel
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerService : Service(), CoroutineScope {

    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var serverSocket: ServerSocket
    private var socketList: ArrayList<Socket?> = arrayListOf()

    private val channel = Channel<Pair<InetAddress, ByteArray>>()

    private lateinit var notificationManager: ChatNotificationManager

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_STOP) {
                onDestroy()
            }
        }
    }

    override fun onCreate() {
        startServer()
        forwardMessage()

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Constants.ACTION_STOP))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = ChatNotificationManager(applicationContext, Constants.FOREGROUND_CHAT_CHANNEL)
        // TODO take the server info to be displayed on the foreground notification
        startForeground(100, notificationManager.foregroundNotification(""))

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
        channel.cancel()
        running = false
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

            if (scanner.hasNext()) {
                val message = scanner.nextLine().toByteArray(Charsets.UTF_8)
                channelSendMessage(socket.localAddress, message)
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
                            if (socket != null) {
                                removeSocket(socket)
                            }
                        }
                    }
                }
            }
        }
    }

    @Synchronized
    private fun channelSendMessage(address: InetAddress, message: ByteArray) {
        launch(Dispatchers.IO) {
            channel.send(Pair(address, message))
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