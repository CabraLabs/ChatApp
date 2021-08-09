package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.utils.ChatManager
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ClientViewModel : ViewModel(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var client: Socket

    private lateinit var userName: String


    private val chatNotification by lazy {
        ChatNotificationManager(applicationContext(), "0")
    }

    private val output by lazy {
        client.getOutputStream()
    }

    fun startSocket(username: String, ip: InetAddress): Boolean {
        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    client = Socket(ip, 1027)
                    userName = username
                }
            }
            true
        } catch (e: java.net.ConnectException) {
            false
        }
    }

    fun getUsername() = userName

    @Synchronized
    fun writeToSocket(message: String): Boolean {
        var success = true

        val messageByte = message.toByteArray(Charsets.UTF_8)

        launch(Dispatchers.IO) {
            success = try {
                output.write(messageByte)
                true
            } catch (e: java.net.SocketException) {
                false
            }
        }

        return success
    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    fun readSocket(background: Boolean = false, activity: Activity? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(client.getInputStream())

            if (scanner.hasNext()) {
                var message = scanner.nextLine().split(";")
                ChatManager.updateRecyclerMessages(message)

                if (background) {
                    chatNotification.sendMessage(message[0], message[1], activity as Activity)
                }
            }
        }
    }

    fun getIpAddress(): String {
        var ip = ""

        runBlocking {
            launch(Dispatchers.IO) {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("8.8.8.8"), 1027)
                    ip = socket.localAddress.hostAddress
                    socket.close()
                }
            }
        }

        return ip
    }

    fun closeSocket() {
        client.close()
    }
}