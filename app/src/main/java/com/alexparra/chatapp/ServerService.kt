package com.alexparra.chatapp

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.alexparra.chatapp.models.ChatNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

class ServerService: Service(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private val serverSocket = ServerSocket(1027)

    override fun onCreate() {
        startServer()
    }

    // TODO create list with accepted sockets.

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationManager = ChatNotificationManager(applicationContext, "1")

        startForeground(1, notificationManager.foregroundNotification(""))

        return START_STICKY
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
            socket.getOutputStream()
        }
    }

    private fun forwardMessage() {
        // TODO foreach on the socket list to send all the images.
    }


    private fun closeServer() {
        serverSocket.close()
    }
}

