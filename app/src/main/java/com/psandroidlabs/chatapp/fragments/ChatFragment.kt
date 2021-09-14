package com.psandroidlabs.chatapp.fragments

import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatAdapter
import com.psandroidlabs.chatapp.databinding.FragmentChatBinding
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.tictactoe.fragments.TicTacToeFragment
import com.psandroidlabs.chatapp.utils.*
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import kotlinx.coroutines.*


@DelicateCoroutinesApi
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
    private var audioName: String = ""

    private lateinit var imageUri: Uri
    private lateinit var imageName: String
    private var imageBitmap: Bitmap? = null

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

    private val registerTakePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            launch(Dispatchers.Default) {
                imageBitmap =
                    PictureManager.getPhotoBitmap(imageUri, requireContext().contentResolver)
                val bitmap = imageBitmap
                if (bitmap != null) {
                    PictureManager.compressBitmap(bitmap, 40)
                }
            }
        }
    }

    private val registerChoosePhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            launch(Dispatchers.Default) {
                imageBitmap = PictureManager.uriToBitmap(uri, requireContext().contentResolver)
                var bitmap = imageBitmap

                if (bitmap != null) {
                    bitmap = PictureManager.compressBitmap(bitmap, 40)
                    imageName = PictureManager.setImageName()
                    PictureManager.bitmapToUri(bitmap, imageName)
                }
            }
        }
    }

    // Fragment life cycle
    override fun onDestroy() {
        this.cancel()
        chatNotification.cancelNotification()

        if (disconnect) {
            val message = ChatManager.leaveMessage(clientUsername)
            ChatManager.addToAdapter(message)

            disconnectSnack.dismiss()
        } else {
            val message = ChatManager.leaveMessage(clientUsername)
            client.writeToSocket(ChatManager.leaveMessage(clientUsername))
            ChatManager.addToAdapter(message)
        }

        client.closeSocket()

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

        activity?.title = getString(R.string.chat_app_bar_name)

        if (!disconnect) {
            activity?.onBackPressedDispatcher?.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        val alertDialog: AlertDialog? = activity?.let {
                            val builder = AlertDialog.Builder(it)
                            builder.apply {
                                setPositiveButton(R.string.yes) { _, _ ->
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
            R.id.ticTactToe -> {
                val ticTacToeFragment = TicTacToeFragment(true)

                //show chat members list and choose one to send invite

                activity?.supportFragmentManager?.let {
                    ticTacToeFragment.show(it, null)
                }

                true
            }

            R.id.shareLink -> {
                client.shareChatLink(requireActivity())
                true
            }

            R.id.chatMembers -> {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startChat()
    }

    private fun startChat() {
        list = ChatManager.chatList
        val recyclerViewList: RecyclerView = binding.chatRecycler
        chatAdapter = ChatAdapter(list, ::onImageClick)

        changeSendButton()
        sendMessageListener()
        recordAudioListener()
        vibrateListener()
        sendImageListener()
        sendPhotoListener()

        client.newMessage.observe(viewLifecycleOwner) {
            notifyAdapterChange(it, true)
        }

        recyclerViewList.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun onImageClick(name: String?, view: View) {
        if (name != null) {
            activity?.supportFragmentManager?.let { PictureManager.showDialogImage(name, it) }
        }
    }

    private fun changeSendButton() {
        with(binding) {
            sendButton.visibility = View.GONE
            recordAudio.visibility = View.VISIBLE

            binding.messageField.doAfterTextChanged {
                if (!it.isNullOrEmpty()) {
                    sendButton.visibility = View.VISIBLE
                    recordAudio.visibility = View.GONE

                    sendPhotoButton.animate().translationX(sendPhotoButton.width.toFloat()).apply {
                        duration = 200
                    }

                    sendImageButton.animate().translationX(sendImageButton.width.toFloat() * 2)
                        .apply {
                            duration = 200
                        }

                    ChatManager.delay(200) {
                        if (messageField.text.toString() != "") {
                            sendPhotoButton.visibility = View.GONE
                            sendImageButton.visibility = View.GONE
                        }
                    }
                } else {
                    sendButton.visibility = View.GONE
                    recordAudio.visibility = View.VISIBLE

                    sendPhotoButton.visibility = View.VISIBLE
                    sendImageButton.visibility = View.VISIBLE

                    sendPhotoButton.animate().translationX(0F).apply {
                        duration = 200
                    }

                    sendImageButton.animate().translationX(0F).apply {
                        duration = 200
                    }
                }
            }
        }
    }

    private fun recordAudioListener() {
        binding.recordAudio.setOnClickListener {
            if (ChatManager.requestPermission(
                    activity,
                    android.Manifest.permission.RECORD_AUDIO,
                    Constants.RECORD_PERMISSION
                )
            )
                recordButton()
        }
    }

    private fun sendPhotoListener() {
        with(binding) {
            sendPhotoButton.setOnClickListener {
                if (ChatManager.requestPermission(
                        activity,
                        android.Manifest.permission.CAMERA,
                        Constants.CAMERA_PERMISSION
                    )
                )
                    takePhoto()
            }
        }
    }

    private fun sendImageListener() {
        with(binding) {
            sendImageButton.setOnClickListener {
                if (ChatManager.requestPermission(
                        activity,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Constants.CHOOSE_IMAGE_GALLERY
                    )
                )
                    choosePicture()
            }
        }
    }

    private fun recordButton() {
        with(binding) {
            recording = !recording

            if (recording) {
                recordAudio.setImageResource(R.drawable.ic_mic_on)

                audioName = RecordAudioManager.nameAudio()

                recorder = RecordAudioManager.createAudioRecorder(
                    RecordAudioManager.audioDir(audioName)
                )

                recorder?.prepare()
                recorder?.start()

                disableChat(true)
            } else {
                recordAudio.setImageResource(R.drawable.ic_mic_none)

                recorder?.apply {
                    stop()
                    release()
                }

                val messageParts = ChatManager.bufferedAudioMessage(clientUsername, audioName)
                notifyAdapterChange(messageParts.first, false)

                launch(Dispatchers.Default) {
                    messageParts.second.forEach {
                        delay(50)
                        client.writeToSocket(it)
                        checkDisconnected(true)
                    }
                }

                audioName = ""
                recorder = null
                disableChat(false)
            }
        }
    }

    private fun vibrateListener() {
        binding.vibrateButton.setOnClickListener {
            val message = ChatManager.vibrateMessage(
                username = clientUsername,
                id = client.id
            )
            client.writeToSocket(message)

            disableAttention()

            checkDisconnected(true, message)
        }
    }

    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {
                val message =
                    ChatManager.parseMessageType(clientUsername, getTextFieldString(), client.id)
                client.writeToSocket(message)

                checkDisconnected(true, message)
            }
        }
    }

    private fun checkDisconnected(success: Boolean, message: Message? = null) {
        if (success) {
            if (message != null) {
                if (message.type != MessageType.AUDIO_MULTIPART.code && message.type != MessageType.IMAGE_MULTIPART.code) {
                    eraseTextField()
                    notifyAdapterChange(message)
                }
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
        if (message?.type != MessageType.AUDIO_MULTIPART.code && message?.type != MessageType.IMAGE_MULTIPART.code) {
            if (message != null) {
                if (received) {
                    ChatManager.addToAdapter(message, received)
                } else {
                    ChatManager.addToAdapter(message)
                }
            }

            binding.chatRecycler.scrollToPosition(list.size - 1)
            chatAdapter.notifyItemChanged(ChatManager.chatList.size)
        }
    }

    private fun takePhoto() {
        imageName = PictureManager.setImageName()
        imageUri = PictureManager.createUri(imageName)
        registerTakePhoto.launch(imageUri)

        val messageParts =
            ChatManager.bufferedImageMessage(clientUsername, imageName)
        notifyAdapterChange(messageParts.first, false)

        messageParts.second.forEach {
            client.writeToSocket(it)
            checkDisconnected(true)
        }
    }

    private fun choosePicture() {
        registerChoosePhoto.launch("image/")

        val messageParts =
            ChatManager.bufferedImageMessage(clientUsername, imageName)
        notifyAdapterChange(messageParts.first, false)

        messageParts.second.forEach {
            client.writeToSocket(it)
            checkDisconnected(true)
        }
    }
}