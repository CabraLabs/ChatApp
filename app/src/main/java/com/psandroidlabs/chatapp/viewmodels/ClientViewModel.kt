package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatMembersAdapter
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*


@DelicateCoroutinesApi
class ClientViewModel : ViewModel(), CoroutineScope {

    private var background = false
    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var userName: String
    var id = 0

    private var socketList: ArrayList<Socket?> = arrayListOf()

    private val chatNotification by lazy {
        ChatNotificationManager(applicationContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    fun background(isBackground: Boolean) {
        background = isBackground
    }

    val accepted: MutableLiveData<AcceptedStatus?> by lazy {
        MutableLiveData<AcceptedStatus?>()
    }

    val newMessage: MutableLiveData<Message> by lazy {
        MutableLiveData<Message>()
    }

    private fun updateMessage(message: Message) {
        newMessage.postValue(message)
    }

    fun updateAccepted(code: AcceptedStatus?) {
        accepted.postValue(code)
    }

    fun getUsername() = userName

    fun startSocket(username: String, ip: InetAddress, port: Int): Boolean {
        try {
            runBlocking {
                launch(Dispatchers.IO) {
                    withTimeout(2000) {
                        val client = Socket()
                        client.connect(InetSocketAddress(ip, port), 2_500)

                        socketList.add(client)
                        userName = username

                        running = true
                    }
                }
            }
        } catch (e: java.net.ConnectException) {
            return false
        } catch (e: java.net.SocketTimeoutException) {
            return false
        }

        return true
    }

    @Synchronized
    fun writeToSocket(message: Message): Boolean {
        message.id = id
        val messageByte = ChatManager.parseToJson(message)

        return try {
            runBlocking {
                launch(Dispatchers.IO) {
                    if (socketList.isNotEmpty()) {
                        socketList[0]?.getOutputStream()?.write(messageByte.toByteArray(Charsets.UTF_8))
                    }
                }
            }

            true
        } catch (e: java.net.SocketException) {
            false
        }
    }

    fun readSocket() {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(socketList[0]?.getInputStream())

            while (isActive && running) {
                if (scanner.hasNextLine()) {
                    val receivedJson = scanner.nextLine()

                    try {
                        val message = ChatManager.serializeMessage(receivedJson)

                        if (message != null) {
                            when (message.type) {
                                MessageType.ACKNOWLEDGE.code -> {
                                    if (message.text != null) {
                                        try {
                                            ChatManager.chatMembersList = ChatManager.serializeProfiles(message.text) as ArrayList<Profile>
                                        } catch (e: JsonDataException) {
                                            Log.e("ClientViewModel", "Server sent an incorrect profile list: ${message.text}")
                                        }
                                    }

                                    if (message.id != null) {
                                        updateAccepted(AcceptedStatus.ACCEPTED)

                                        id = message.id
                                            ?: throw Exception(
                                                "id cannot be changed to null, it is useful to have it as nullable but changing it's value to null is a mistake."
                                            )
                                    } else {
                                        updateAccepted(AcceptedStatus.MISSING_ID)
                                    }
                                }

                                MessageType.REVOKED.code -> {
                                    when (message.id) {
                                        AcceptedStatus.WRONG_PASSWORD.code -> updateAccepted(AcceptedStatus.WRONG_PASSWORD)
                                        AcceptedStatus.SECURITY_KICK.code -> updateAccepted(AcceptedStatus.SECURITY_KICK)
                                        AcceptedStatus.ADMIN_KICK.code -> updateAccepted(AcceptedStatus.ADMIN_KICK)
                                    }

                                    closeSocket()
                                }

                                else -> {
                                    if (accepted.value == AcceptedStatus.ACCEPTED) {
                                        when (message.type) {
                                            MessageType.VIBRATE.code -> {
                                                ChatManager.startVibrate()
                                            }

                                            MessageType.JOIN.code -> {
                                                //TODO add profile to ChatManager.chatMembersList
                                            }
                                        }

                                        if (background) {
                                            chatNotification.sendMessage(
                                                message.username ?: "",
                                                message.text ?: ""
                                            )
                                        }
                                    }

                                    updateMessage(message)
                                }
                            }
                        }
                    } catch (e: JsonDataException) {
                        Log.e("ClientViewModel", receivedJson)
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

    fun shareChatLink(activity: Activity) {
        val sendIntent: Intent = Intent().apply {
            val ip = socketList[0]?.localAddress.toString()

            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "http://www.chatapp.psandroidlabs.com/args=${ip.replace("/", "")}:${socketList[0]?.port}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }

    fun showChatMembers(context: Context?) {
        if (context != null) {
            val dialogBox = Dialog(context).apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                setContentView(R.layout.fragment_chat_members)
                setCanceledOnTouchOutside(true)
                setCancelable(true)
                show()
            }

            val recyclerView: RecyclerView = dialogBox.findViewById(R.id.membersRecycler)
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }

            val adapter = ChatMembersAdapter(ChatManager.chatMembersList, ::onClick)
            recyclerView.adapter = adapter
        }
    }

    private fun onClick(pos: Int) {
        TODO()
    }
}