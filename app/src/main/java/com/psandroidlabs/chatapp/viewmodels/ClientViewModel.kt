package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
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
        ChatNotificationManager(applicationContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    private val output by lazy {
        client.getOutputStream()
    }

    fun startSocket(username: String, ip: InetAddress): Boolean {
        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    client = Socket(ip, Constants.CHAT_DEFAULT_PORT)
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
    fun readSocket(background: Boolean = false, activity: Activity? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(client.getInputStream())

            if (scanner.hasNext()) {
                val message = Message(scanner.nextLine().split(";"))

                ChatManager.addToAdapter(message, true)

                if (background) {
                    chatNotification.sendMessage(message.username, message.message, activity as Activity)
                }
            }
        }
    }

    fun getIpAddress(): String {
        var ip = ""

        runBlocking {
            launch(Dispatchers.IO) {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("8.8.8.8"), Constants.CHAT_DEFAULT_PORT)
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