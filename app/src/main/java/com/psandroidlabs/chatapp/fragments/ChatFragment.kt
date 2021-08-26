package com.psandroidlabs.chatapp.fragments

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.databinding.FragmentChatBinding
import com.psandroidlabs.chatapp.models.ChatNotificationManager
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import kotlinx.coroutines.*


class ChatFragment : Fragment(), CoroutineScope {

    private val client: ClientViewModel by activityViewModels()
    private val arg: ChatFragmentArgs by navArgs()
    private lateinit var binding: FragmentChatBinding

    private lateinit var chatAdapter: ChatAdapter

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private var list: ArrayList<Message> = ArrayList()

    private var recording = false
    private var disconnect = false

    private var recorder: MediaRecorder? = null
    private var audioName: String? = null

    private val chatNotification by lazy {
        ChatNotificationManager(requireContext(), Constants.PRIMARY_CHAT_CHANNEL)
    }

    private val navController: NavController by lazy {
        findNavController()
    }

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
                client.updateAccepted(null)
                navController.popBackStack()
            }
    }

    // Fragment life cycle
    override fun onDestroy() {
        client.closeSocket()
        this.cancel()
        chatNotification.cancelNotification()

        if (disconnect) {
            disconnectSnack.dismiss()
        }

        if (arg.user == UserType.SERVER) {
            activity?.title = getString(R.string.server_app_bar_name)
        } else {
            activity?.title = getString(R.string.client_app_bar_name)
        }

        super.onDestroy()
    }

    override fun onResume() {
        client.background(false)
        chatNotification.cancelNotification()
        super.onResume()
    }

    override fun onPause() {
        recorder?.let {
            it.stop()
            it.release()
        }

        recorder = null

        client.background(true)
        super.onPause()
    }

    // App Bar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        ChatManager.getFragmentActivity(activity)
        activity?.title = getString(R.string.chat_app_bar_name)

        if (!disconnect) {
            activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val alertDialog: AlertDialog? = activity?.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setPositiveButton(R.string.yes) { _, _ ->
                                val message = ChatManager.leaveMessage(clientUsername)

                                client.writeToSocket(ChatManager.leaveMessage(clientUsername))
                                ChatManager.addToAdapter(message)

                                client.updateAccepted(null)
                                navController.popBackStack()
                            }

                            setNegativeButton(R.string.no) { _, _ ->

                            }
                        }

                        builder.setTitle(getString(R.string.dialog_warning))
                        builder.setCancelable(true)

                        builder.create()
                    }

                    alertDialog?.show()
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.ticTactToe -> {
//                val ticTacToeFragment = TicTacToeFragment(true)
//
//                GlobalScope.launch(Dispatchers.IO) {
//                    client.writeToSocket(
//                        ChatManager.sendMessageToSocket(
//                            clientUsername,
//                            "/TICTACTOE_INVITE"
//                        )
//                    )
//                }
//
//                activity?.supportFragmentManager?.let {
//                    ticTacToeFragment.show(it, null)
//                }
//
//                true
//            }

            R.id.shareLink -> {
                client.shareChatLink(requireActivity())
                true
            }

            R.id.chatMembers -> {
                //TODO call chat members dialog
                client.showChatMembers(context)
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startChat()
    }

    @DelicateCoroutinesApi
    private fun startChat() {
        list = ChatManager.chatList
        val recyclerViewList: RecyclerView = binding.chatRecycler
        chatAdapter = ChatAdapter(list)

        notifyAdapterChange()
        changeSendButton()
        sendMessageListener()
        recordAudioListener()
        vibrateListener()

        val messageObserver = Observer<Message> {
            notifyAdapterChange(it, true)
        }
        client.newMessage.observe(viewLifecycleOwner, messageObserver)

        recyclerViewList.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun changeSendButton() {
        with(binding) {
            sendButton.visibility = View.GONE
            recordAudio.visibility = View.VISIBLE

            binding.messageField.doAfterTextChanged {
                if (it.toString().isNotEmpty()) {
                    sendButton.visibility = View.VISIBLE
                    recordAudio.visibility = View.GONE
                } else {
                    sendButton.visibility = View.GONE
                    recordAudio.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun recordAudioListener() {
        binding.recordAudio.setOnClickListener {
            requestPermission()
        }
    }

    private fun recordButton() {
        with(binding) {
            recording = !recording

            if (recording) {
                recordAudio.setBackgroundColor(requireContext().getColor(R.color.red))

                audioName = ChatManager.nameAudio()
                recorder = ChatManager.createAudioRecorder(requireContext(), audioName)

                recorder?.prepare()
                recorder?.start()
                disableChat(true)
            } else {
                recordAudio.setBackgroundColor(requireContext().getColor(R.color.black))

                recorder?.apply {
                    stop()
                    release()
                }

                val message = ChatManager.audioMessage(clientUsername, audioName)
                val success = client.writeToSocket(message)

                checkDisconnected(success)

                audioName = null
                recorder = null
                disableChat(false)
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                RECORD_AUDIO
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(RECORD_AUDIO),
                Constants.RECORD_PERMISSION
            )
        } else {
            recordButton()
        }
    }


    private fun vibrateListener() {
        binding.vibrateButton.setOnClickListener {
            val message = ChatManager.vibrateMessage(
                username = clientUsername,
                id = client.id
            )
            val success = client.writeToSocket(message)

            disableAttention()

            checkDisconnected(success, message)
        }
    }

    @DelicateCoroutinesApi
    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {
                val message = ChatManager.parseMessageType(clientUsername, getTextFieldString(), client.id)
                val success = client.writeToSocket(message)

                checkDisconnected(success, message)
            }
        }
    }

    private fun checkDisconnected(success: Boolean, message: Message? = null) {
        if (success) {
            if (message != null) {
                eraseTextField()
                notifyAdapterChange(message)
            }
        } else {
            disconnectedSnackBar()
        }
    }

    private fun disableChat(isDisabled: Boolean) {
        binding.messageField.apply {
            isClickable = isDisabled
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

    private fun disconnectedSnackBar() {
        disconnect = true
        disableChat(true)
        disconnectSnack.show()
    }

    private fun getTextFieldString() = binding.messageField.text.toString()

    private fun eraseTextField() {
        binding.messageField.setText("")
    }

    private fun notifyAdapterChange(message: Message? = null, received: Boolean = false) {
        if (message != null) {
            if (received) {
                ChatManager.addToAdapter(message, received)
            } else {
                ChatManager.addToAdapter(message)
            }
        }

        binding.chatRecycler.scrollToPosition(list.size - 1)
        chatAdapter.notifyDataSetChanged()
    }
}