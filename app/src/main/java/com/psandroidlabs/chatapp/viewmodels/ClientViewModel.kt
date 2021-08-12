package com.psandroidlabs.chatapp.viewmodels

import androidx.lifecycle.ViewModel
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.Socket
import java.util.*

class ClientViewModel : ViewModel(), CoroutineScope {

    private var background = false
    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var userName: String
    private var id = 0

    private var socketList: ArrayList<Socket?> = arrayListOf()

    private val chatNotification by lazy {
        ChatNotificationManager(applicationContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    fun background(isBackground: Boolean) {
        background = isBackground
    }

    fun getUsername() = userName

    fun startSocket(username: String, ip: InetAddress): Boolean {
        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    val client = Socket(ip, Constants.CHAT_DEFAULT_PORT)
                    socketList.add(client)
                    userName = username
                }
                running = true
            }
            true
        } catch (e: java.net.ConnectException) {
            false
        }
    }

    @Synchronized
    fun writeToSocket(message: Message): Boolean {
        var success = true

        message.id = id
        val messageByte = ChatManager.parseToJson(message).toByteArray(Charsets.UTF_8)

        launch(Dispatchers.IO) {
            success = try {
                socketList[0]?.getOutputStream()?.write(messageByte)
                true
            } catch (e: java.net.SocketException) {
                false
            }
        }

        return success
    }

    @DelicateCoroutinesApi
    fun readSocket(chatAdapter: ChatAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(socketList[0]?.getInputStream())

            while (isActive && running) {
                if (scanner.hasNextLine()) {
                    val receivedJson = scanner.nextLine()

                    val message = ChatManager.serializeMessage(receivedJson)

                    // TODO make the exception be changed to a proper handle with a message to the user.
                    if (message != null) {
                        if (message.type == MessageType.ACKNOWLEDGE.code) {
                            id = message.id ?: throw Exception("Server failed to send a verification Id")

                        } else if (message.type == MessageType.REVOKED.code) {
                            // TODO close connection and display message to the user
                            closeSocket()

                        } else {
                            ChatManager.addToAdapter(message, true)

                            withContext(Dispatchers.Main) {
                                chatAdapter.notifyDataSetChanged()
                            }

                            // TODO track this
                            if (background) {
                                ChatManager.playSound()
                                chatNotification.sendMessage(message.username, message.text ?: "")
                            }
                        }
                    }
                }
            }
        }
    }

    fun transformIp(text: String): InetAddress = InetAddress.getByName(text)

    fun closeSocket() {
        running = false
        socketList[0]?.close()
        socketList.remove(socketList[0])
    }
}