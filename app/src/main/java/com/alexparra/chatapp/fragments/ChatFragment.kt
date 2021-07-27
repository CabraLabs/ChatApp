package com.alexparra.chatapp.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.adapters.ChatAdapter
import com.alexparra.chatapp.databinding.FragmentChatBinding
import com.alexparra.chatapp.models.ChatNotificationManager
import com.alexparra.chatapp.models.Message
import com.alexparra.chatapp.models.MessageType
import com.alexparra.chatapp.utils.ChatManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*


@RequiresApi(Build.VERSION_CODES.O)
class ChatFragment : Fragment(), CoroutineScope {

    var list: ArrayList<Message> = ArrayList()

    private val args: ChatFragmentArgs by navArgs()

    private lateinit var binding: FragmentChatBinding

    private lateinit var chatAdapter: ChatAdapter


    private val navController: NavController by lazy {
        findNavController()
    }

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private val CHAT_CHANNEL = "0"

    private val chatNotification by lazy {
        ChatNotificationManager(requireContext(), CHAT_CHANNEL)
    }

    override fun onDestroy() {
        args.connection.closeSocket()
        this.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        chatNotification.cancelNotification()
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPause() {
        receiveMessageListener(true)
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startChat()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startChat() {
        list = ChatManager.chatList
        val recyclerViewList: RecyclerView = binding.chatRecycler
        chatAdapter = ChatAdapter(list)

        val connectMessage = ChatManager.connectMessage(args.connection, requireContext())

        list.add(connectMessage)

        sendConnectMessage(connectMessage)
        receiveMessageListener()
        sendMessageListener()
        vibrateListener()

        recyclerViewList.apply {
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun vibrateListener() {
        binding.btnVibrate.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                args.connection.writeToSocket(
                    ChatManager.sendMessageToSocket(
                        args.connection,
                        "/vibrate"
                    )
                )
                disableAttention()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startVibrate() {
        val vibrator = getSystemService(requireContext(), Vibrator::class.java)
        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                1000,
                VibrationEffect.EFFECT_HEAVY_CLICK
            )
        )
    }

    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        args.connection.writeToSocket(
                            ChatManager.sendMessageToSocket(
                                args.connection,
                                getTextFieldString()
                            )
                        )
                        eraseTextField()
                    } catch (e: java.net.SocketException) {
                        withContext(Dispatchers.Main) {
                            disableChat()
                            Snackbar.make(view as View, getString(R.string.snack_server_disconnect), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Exit Chat") {
                                    onDestroy()
                                    navController.popBackStack()
                                }.show()
                        }
                    }
                }

                list.add(ChatManager.getSentMessage(args.connection, getTextFieldString()))
            }

            notifyAdapterChange()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun receiveMessageListener(background: Boolean = false) {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = args.connection.updateSocket()

            while (scanner.hasNextLine()) {
                // [0] Username | [1] Message | [2] Time | [3] Joined
                val message = scanner.nextLine().split(";")

                withContext(Dispatchers.Main) {
                    if (background) {
                        chatNotification.sendMessage(message[0], message[1])
                    }

                    when {
                        message[1] == "/vibrate" -> {
                            startVibrate()
                            ChatManager.chatList.add(
                                Message(
                                    MessageType.ATTENTION,
                                    message[0],
                                    message[1],
                                    message[2]
                                )
                            )
                        }

                        message[3].isNotBlank() -> {
                            ChatManager.chatList.add(
                                Message(
                                    MessageType.JOINED,
                                    message[0],
                                    message[1],
                                    message[2]
                                )
                            )
                        }

                        else -> {
                            ChatManager.chatList.add(
                                Message(
                                    MessageType.RECEIVED,
                                    message[0],
                                    message[1],
                                    message[2]
                                )
                            )
                        }
                    }

                    notifyAdapterChange()
                }
            }
        }
    }

    private fun sendConnectMessage(message: Message) {
        GlobalScope.launch(Dispatchers.IO) {
            val sendMessage =
                "${message.username};${message.message};${message.time};${message.type}\n"
            args.connection.writeToSocket(sendMessage)
            notifyAdapterChange()
        }
    }

    private fun disableChat() {
        binding.messageField.apply {
            alpha = 0.3f
            isClickable = false
        }
    }

    private fun disableAttention() {
        with(binding) {
            btnVibrate.apply {
                alpha = 0.2F
                isClickable = false

                ChatManager.delay(5000) {
                    alpha = 1F
                    isClickable = true
                }
            }
        }
    }

    private fun getTextFieldString() = binding.messageField.text.toString()

    private fun eraseTextField() {
        binding.messageField.setText("")
    }

    private fun notifyAdapterChange() {
        chatAdapter.notifyDataSetChanged()
    }
}