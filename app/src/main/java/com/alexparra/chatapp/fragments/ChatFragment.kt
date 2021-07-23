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
import com.alexparra.chatapp.adapters.ChatAdapter
import com.alexparra.chatapp.databinding.FragmentChatBinding
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.models.Server
import com.alexparra.chatapp.utils.ChatManager
import kotlinx.coroutines.*


class ChatFragment : Fragment(), CoroutineScope {

    private val args: ChatFragmentArgs by navArgs()

    var list: ArrayList<String> = ArrayList()

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
        chatAdapter = ChatAdapter(list, args.connection)

        list.add(connectMessage())

        updateMessageScreenListener()

        sendMessageListener()

        recyclerViewList.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getText().isNotBlank()) {
                launch(Dispatchers.IO) {
                    args.connection.writeToSocket(getText())

                    eraseTextField()
                }

                when (args.connection) {
                    is ClientSocket -> {
                        list.add("${getText()}")
                    }

                    is Server -> {
                        list.add("${getText()}")
                    }
                }
            }

            notifyAdapterChange()
        }
    }

    private fun updateMessageScreenListener() {
        launch(Dispatchers.IO) {
            val scanner = args.connection.updateSocket()

            while (scanner.hasNextLine()) {
                ChatManager.chatList.add("s;${"received"};${scanner.nextLine()};${ChatManager.currentTime()}")

                withContext(Dispatchers.Main) {
                    notifyAdapterChange()
                }
            }
        }
    }

    private fun getText() = "${args.connection.username};${binding.messageField.text};${ChatManager.currentTime()}\n"

    private fun connectMessage(): String {
        with(args) {
            if (connection is ClientSocket) {
                return "${connection.username};joined the room;${ChatManager.currentTime()}"
            }

            return "${connection.username};created the room at;${ChatManager.currentTime()}"
        }
    }

    private fun eraseTextField() {
        binding.messageField.setText("")
    }

    private fun notifyAdapterChange() {
        chatAdapter.notifyDataSetChanged()
    }
}