package com.psandroidlabs.chatapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
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

    private var SERVER_START = false
    private var running = true

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var serverSocket: ServerSocket
    private var socketList: ArrayList<Socket?> = arrayListOf()

    private val channel = Channel<Pair<InetAddress, ByteArray>>()

    private lateinit var notificationManager: ChatNotificationManager

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_STOP) {
                onDestroy()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        startServer()
        forwardMessage()

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Constants.ACTION_STOP))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = ChatNotificationManager(applicationContext, "foreground")
        startForeground(100, notificationManager.foregroundNotification(""))

        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        if (SERVER_START) {
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

    @RequiresApi(Build.VERSION_CODES.O)
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