package com.psandroidlabs.chatapp.fragments

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
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.databinding.FragmentChatBinding
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.tictactoe.fragments.TictactoeFragment
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.utils.Constants
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class ChatFragment : Fragment(), CoroutineScope {

    private val arg: ChatFragmentArgs by navArgs()
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private var list: ArrayList<Message> = ArrayList()

    private var background = false

    private val chatNotification by lazy {
        ChatNotificationManager(requireContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    private val navController: NavController by lazy {
        findNavController()
    }

    private val client: ClientViewModel by activityViewModels()

    private val clientUsername: String by lazy {
        client.getUsername()
    }

    private val disconnectSnack: Snackbar by lazy {
        Snackbar.make(
            view as View,
            getString(R.string.snack_server_disconnect),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Exit Chat") {
                onDestroy()
                navController.popBackStack()
            }
    }


    // Fragment life cycle
    override fun onDestroy() {
        client.closeSocket()
        this.cancel()
        chatNotification.cancelNotification()
        disconnectSnack.dismiss()

        if (arg.user == UserType.SERVER) {
            activity?.title = getString(R.string.server_app_bar_name)
        } else {
            activity?.title = getString(R.string.client_app_bar_name)
        }

        super.onDestroy()
    }

    override fun onResume() {
        background = false
        chatNotification.cancelNotification()
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPause() {
        background = true
        super.onPause()
    }

    // App Bar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        ChatManager.getFragmentActivity(activity)
        activity?.title = getString(R.string.chat_app_bar_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ticTactToe -> {

                //TODO send invite to specific user and wait to start the game

                val ticTacToeFragment = TictactoeFragment(true)

                GlobalScope.launch(Dispatchers.IO) {
                    client.writeToSocket(
                        ChatManager.sendMessageToSocket(
                            clientUsername,
                            "/TICTACTOE_INVITE"
                        )
                    )
                }

                activity?.supportFragmentManager?.let {
                    ticTacToeFragment.show(it, null)
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

        val connectMessage = ChatManager.connectMessage(arg.user, client.getUsername(), requireContext())
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
        val vibrate = "/vibrate"

        binding.vibrateButton.setOnClickListener {
            val success = client.writeToSocket(ChatManager.sendMessageToSocket(clientUsername, vibrate))

            if (success) {
                ChatManager.sendVibrateMessage(clientUsername)
                notifyAdapterChange()
                disableAttention()
            } else {
                disconnectedSnackBar()
            }
        }
    }

    @DelicateCoroutinesApi
    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {

                val success = client.writeToSocket(ChatManager.sendMessageToSocket(clientUsername, getTextFieldString()))

                if (success) {
                    eraseTextField()
                } else {
                    disconnectedSnackBar()
                }

                list.add(ChatManager.getSentMessage(clientUsername, getTextFieldString()))
            }

            notifyAdapterChange()
        }
    }

    @DelicateCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun receiveMessageListener() {
        if (background) {
            client.readSocket(true, activity)
        } else {
            client.readSocket()
        }
    }

    // TODO
    @DelicateCoroutinesApi
    private fun sendConnectMessage(message: Message) {
        val sendMessage = "${message.username};${message.message};${message.time};${message.type}\n"
        client.writeToSocket(sendMessage)
        notifyAdapterChange()
    }

    private fun disableChat() {
        binding.messageField.apply {
            alpha = 0.3F
            isClickable = false
        }
    }

    private fun disableAttention() {
        // TODO CHECK IF DISABLE CHAT DISABLES THIS
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

    private fun disconnectedSnackBar() {
        disableChat()
        disconnectSnack.show()
    }

    private fun getTextFieldString() = binding.messageField.text.toString()

    private fun eraseTextField() {
        binding.messageField.setText("")
    }

    private fun notifyAdapterChange() {
        chatAdapter.notifyDataSetChanged()
    }
}