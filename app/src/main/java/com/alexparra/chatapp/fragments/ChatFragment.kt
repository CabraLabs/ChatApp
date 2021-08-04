package com.alexparra.chatapp.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.adapters.ChatAdapter
import com.alexparra.chatapp.databinding.FragmentChatBinding
import com.alexparra.chatapp.models.ChatNotificationManager
import com.alexparra.chatapp.models.Message
import com.alexparra.chatapp.models.UserType
import com.alexparra.chatapp.tictactoe.fragments.TictactoeFragment
import com.alexparra.chatapp.tictactoe.utils.TictactoeManager
import com.alexparra.chatapp.utils.ChatManager
import com.alexparra.chatapp.utils.ChatManager.updateRecyclerMessages
import com.alexparra.chatapp.viewmodels.ClientViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.O)
class ChatFragment : Fragment(), CoroutineScope {

    private val arg: ChatFragmentArgs by navArgs()
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    var list: ArrayList<Message> = ArrayList()
    private var BACKGROUND = false
    private val CHAT_CHANNEL = "0"

    private val chatNotification by lazy {
        ChatNotificationManager(requireContext(), CHAT_CHANNEL)
    }

    private val navController: NavController by lazy {
        findNavController()
    }

    private val client: ClientViewModel by activityViewModels()

    // Fragment life cycle
    override fun onDestroy() {
        args.connection.closeSocket()
        this.cancel()
        chatNotification.cancelNotification()

        if (arg.user == UserType.SERVER) {
            activity?.title = getString(R.string.server_app_bar_name)
        } else {
            activity?.title = getString(R.string.client_app_bar_name)
        }

        super.onDestroy()
    }

    override fun onResume() {
        BACKGROUND = false
        chatNotification.cancelNotification()
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPause() {
        BACKGROUND = true
        super.onPause()
    }

    // App Bar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        activity?.title = getString(R.string.chat_app_bar_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ticTactToe -> {
                val currentBoard = TictactoeManager.board
                val tictactoeFragment = TictactoeFragment(currentBoard, args.connection)

                activity?.supportFragmentManager?.let {
                    tictactoeFragment.show(it, null)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startChat()
    }

    @DelicateCoroutinesApi
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
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    @DelicateCoroutinesApi
    private fun vibrateListener() {
        binding.vibrateButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                args.connection.writeToSocket(
                    ChatManager.sendMessageToSocket(
                        args.connection,
                        "/vibrate"
                    )
                )
                withContext(Dispatchers.Main) {
                    ChatManager.sendVibrateMessage(args.connection)
                    notifyAdapterChange()
                    disableAttention()
                }
            }
        }
    }

    @DelicateCoroutinesApi
    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {

                var success = client.writeToSocket(ChatManager.sendMessageToSocket(client.getUsername(), getTextFieldString()))

                if (success) {
                    eraseTextField()
                } else {
                    disableChat()
                    Snackbar.make(
                        view as View,
                        getString(R.string.snack_server_disconnect),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Exit Chat") {
                            onDestroy()
                            navController.popBackStack()
                        }.show()
                }

                if (BACKGROUND) {
                    chatNotification.sendMessage(message[0], message[1], activity as Activity)
                }

                list.add(ChatManager.getSentMessage(args.connection, getTextFieldString()))
            }

            notifyAdapterChange()
        }

    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun receiveMessageListener() {
        GlobalScope.launch(Dispatchers.IO) {
            val scanner = args.connection.readSocket()

            while (scanner.hasNextLine()) {
                // [0] Username | [1] Message | [2] Time | [3] Joined
                val message = scanner.nextLine().split(";")

                withContext(Dispatchers.Main) {
                    if (BACKGROUND) {
                        chatNotification.sendMessage(message[0], message[1], activity as Activity)
                    }

                    updateRecyclerMessages(message)

                    notifyAdapterChange()
                }
            }
        }
    }

    @DelicateCoroutinesApi
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
            alpha = 0.3F
            isClickable = false
        }
    }

    private fun disableAttention() {
        with(binding) {
            vibrateButton.apply {
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