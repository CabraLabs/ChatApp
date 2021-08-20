package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.models.AcceptedStatus
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.Socket
import java.util.*
import java.util.regex.Pattern

class ClientViewModel : ViewModel(), CoroutineScope {

    private var background = false
    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var userName: String
    var id = 0

    private var socketList: ArrayList<Socket?> = arrayListOf()

    private lateinit var chatAdapter: ChatAdapter

    private val chatNotification by lazy {
        ChatNotificationManager(applicationContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    fun background(isBackground: Boolean) {
        background = isBackground
    }

    val accepted: MutableLiveData<AcceptedStatus> by lazy {
        MutableLiveData<AcceptedStatus>()
    }

    private fun updateAccepted(code: AcceptedStatus) {
        accepted.postValue(code)
    }

    fun initAdapter(adapter: ChatAdapter) {
        chatAdapter = adapter
    }

    fun getUsername() = userName

    fun startSocket(username: String, ip: InetAddress, port: Int): Boolean {
        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    val client = Socket(ip, port)
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
        message.id = id
        val messageByte = ChatManager.parseToJson(message)

        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    socketList[0]?.getOutputStream()?.write(messageByte.toByteArray(Charsets.UTF_8))
                }
            }
            true
        } catch (e: java.net.SocketException) {
            false
        }
    }

    @DelicateCoroutinesApi
    fun readSocket(chatAdapter: ChatAdapter? = null) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(socketList[0]?.getInputStream())

            while (isActive && running) {
                if (scanner.hasNextLine()) {
                    val receivedJson = scanner.nextLine()

                    val message = ChatManager.serializeMessage(receivedJson)

                    // TODO make the exception be changed to a proper handle with a message to the user.
                    if (message != null) {
                        if (message.type == MessageType.ACKNOWLEDGE.code) {
                            updateAccepted(AcceptedStatus.ACCEPTED)

                            id = message.id
                                ?: throw Exception("Server failed to send a verification Id")

                        } else if (message.type == MessageType.REVOKED.code) {
                            when (message.id) {
                                AcceptedStatus.WRONG_PASSWORD.code -> updateAccepted(AcceptedStatus.WRONG_PASSWORD)
                                AcceptedStatus.SECURITY_KICK.code -> updateAccepted(AcceptedStatus.SECURITY_KICK)
                                AcceptedStatus.ADMIN_KICK.code -> updateAccepted(AcceptedStatus.ADMIN_KICK)
                            }

                            closeSocket()

                        } else {
                            ChatManager.addToAdapter(message, true)

                            withContext(Dispatchers.Main) {
                                chatAdapter?.notifyDataSetChanged()
                            }

                            // TODO track this
                            if (background) {
                                ChatManager.playSound()
                                chatNotification.sendMessage(
                                    message.username ?: "",
                                    message.text ?: ""
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun transformIp(text: String): InetAddress = InetAddress.getByName(text)

    fun validateIp(text: String): Boolean {
        val ipRegex = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";
        return Pattern.matches(ipRegex, text)
    }

    fun closeSocket() {
        running = false
        socketList[0]?.close()
        socketList.remove(socketList[0])
    }

    fun shareChatLink(activity: Activity) {
        //TODO server IP in putExtra
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_INTENT, "http/www.chatapp.psandroidlabs.com/clientconnect/chatroomip=10")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }
}