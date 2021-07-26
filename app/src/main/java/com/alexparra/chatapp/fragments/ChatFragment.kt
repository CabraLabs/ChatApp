package com.alexparra.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.adapters.ChatAdapter
import com.alexparra.chatapp.databinding.FragmentChatBinding
import com.alexparra.chatapp.models.Message
import com.alexparra.chatapp.models.MessageType
import com.alexparra.chatapp.utils.ChatManager
import kotlinx.coroutines.*


class ChatFragment : Fragment(), CoroutineScope {

    var list: ArrayList<Message> = ArrayList()

    private val args: ChatFragmentArgs by navArgs()

    private lateinit var binding: FragmentChatBinding

    private lateinit var chatAdapter: ChatAdapter

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    // TODO MAKE ON BACK PRESSED
    override fun onDestroy() {
        args.connection.closeSocket()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startChat()
    }

    private fun startChat() {
        list = ChatManager.chatList
        val recyclerViewList: RecyclerView = binding.chatRecycler
        chatAdapter = ChatAdapter(list)

        val connectMessage = ChatManager.connectMessage(args.connection, requireContext())

        list.add(connectMessage)

        sendConnectMessage(connectMessage)
        receiveMessageListener()
        sendMessageListener()

        recyclerViewList.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {
                launch(Dispatchers.IO) {
                    args.connection.writeToSocket(ChatManager.sendMessageToSocket(args.connection, getTextFieldString()))
                    eraseTextField()
                }
                list.add(ChatManager.getSentMessage(args.connection, getTextFieldString()))
            }
            notifyAdapterChange()
        }
    }

    private fun receiveMessageListener() {
        launch(Dispatchers.IO) {
            val scanner = args.connection.updateSocket()

            while (scanner.hasNextLine()) {
                // [0] Username | [1] Message | [2] Time | [3] Joined
                var message = scanner.nextLine().split(";")

                withContext(Dispatchers.Main) {
                    if (message[4].isBlank()) {
                        ChatManager.chatList.add(Message(MessageType.JOINED, message[0], message[1], message[2]))
                    } else {
                        ChatManager.chatList.add(Message(MessageType.RECEIVED, message[0], message[1], message[2]))

                    }

                    notifyAdapterChange()
                }
            }
        }
    }

    private fun sendConnectMessage(message: Message) {
        launch(Dispatchers.IO) {
            val sendMessage = "${message.username};${message.message};${message.time};${message.type}"
            args.connection.writeToSocket(sendMessage)
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