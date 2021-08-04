package com.alexparra.chatapp.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import com.alexparra.chatapp.MainApplication
import com.alexparra.chatapp.models.ChatNotificationManager
import com.alexparra.chatapp.utils.ChatManager
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ServerViewModel : AndroidViewModel(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private val serverSocket = ServerSocket(1027)

    private lateinit var server: Socket

    private val username = "admin"

    private val chatNotification by lazy {
        ChatNotificationManager(MainApplication.applicationContext(), "0")
    }

    fun startServer(): Boolean {
        return try {
            server = serverSocket.accept()
            true
        } catch (e: java.net.BindException) {
            false
        }
    }

    fun getUsername() = username

    fun writeToSocket(message: String) {
        val output = server.getOutputStream()
        val messageByte = message.toByteArray(Charsets.UTF_8)
        launch(Dispatchers.IO) {
            output.write(messageByte)
        }
    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    fun readSocket(background: Boolean = false) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(server.getInputStream())

            if (scanner.hasNext()) {
                var message = scanner.nextLine().split(";")
                ChatManager.updateRecyclerMessages(message)


                if (background) {
                    chatNotification.sendMessage(message[0], message[1])
                }
            }
        }
    }

    fun getIpAddress(): String {
        var ip = ""
        launch(Dispatchers.IO) {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 1027)
                ip = socket.localAddress.hostAddress.toString()
                socket.close()
            }
        }

        return ip
    }

    fun closeServer() {
        server.close()
    }
}