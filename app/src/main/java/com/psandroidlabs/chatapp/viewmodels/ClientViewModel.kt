package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.adapters.ChatMembersAdapter
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import com.psandroidlabs.chatapp.utils.toast
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.Socket
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ClientViewModel : ViewModel(), CoroutineScope {

    private var background = false
    private var running = false

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private lateinit var userName: String
    var id = 0

    private var socketList: ArrayList<Socket?> = arrayListOf()

    lateinit var chatAdapter: ChatAdapter
    lateinit var chatRecyclerView: RecyclerView


    private val chatNotification by lazy {
        ChatNotificationManager(applicationContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    fun background(isBackground: Boolean) {
        background = isBackground
    }

    val accepted: MutableLiveData<AcceptedStatus?> by lazy {
        MutableLiveData<AcceptedStatus?>()
    }

    fun updateAccepted(code: AcceptedStatus?) {
        accepted.postValue(code)
    }

    fun initializeChatRecyclerView(recyclerView: RecyclerView) {
        chatRecyclerView = recyclerView
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
    fun readSocket() {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = Scanner(socketList[0]?.getInputStream())

            while (isActive && running) {
                if (scanner.hasNextLine()) {
                    val receivedJson = scanner.nextLine()

                    val message = ChatManager.serializeMessage(receivedJson)

                    // TODO make the exception be changed to a proper handle with a message to the user.
                    if (message != null) {
                        when(message.type){
                            MessageType.ACKNOWLEDGE.code -> {
                                updateAccepted(AcceptedStatus.ACCEPTED)

                                // TODO handle exception when the server doesn't send anybody on the list.
                                if (message.text != null) {
                                    ChatManager.chatMembersList = ChatManager.serializeProfiles(message.text) as ArrayList<Profile>
                                }

                                id = message.id
                                    ?: throw Exception("Server failed to send a verification Id")
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
                                ChatManager.addToAdapter(message, true)

                                withContext(Dispatchers.Main) {
                                    if (accepted.value == AcceptedStatus.ACCEPTED) {
                                        ChatManager.scrollChat(chatRecyclerView)
                                        chatAdapter?.notifyDataSetChanged()
                                    }

                                    when(message.type) {
                                        MessageType.VIBRATE.code -> {
                                            ChatManager.startVibrate()
                                        }
                                    }

                                    if (background) {
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
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "http/www.chatapp.psandroidlabs.com/clientconnect/chatroomip=${socketList[0]?.localAddress}"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        activity.startActivity(shareIntent)
    }

    fun showChatMembers(context: Context?) {
        if(context != null){
            val dialogBox = Dialog(context)
            dialogBox.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBox.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialogBox.setContentView(R.layout.fragment_chat_members)
            dialogBox.setCanceledOnTouchOutside(true)
            dialogBox.setCancelable(true)
            dialogBox.show()

            val recyclerView: RecyclerView = dialogBox.findViewById(R.id.membersRecycler)
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )

            val adapter = ChatMembersAdapter(ChatManager.chatMembersList, ::onClick)
            recyclerView.adapter = adapter
        }
    }

    private fun onClick(pos: Int){
        TODO()
    }

}