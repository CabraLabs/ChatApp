package com.psandroidlabs.chatapp.fragments

import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
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
import com.psandroidlabs.chatapp.tictactoe.fragments.TicTacToeFragment
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import com.psandroidlabs.chatapp.utils.PictureManager
import com.psandroidlabs.chatapp.utils.RecordAudioManager
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
            val message = ChatManager.imageMessage(clientUsername, imageUri.path, PictureManager.uriToBitmap(imageUri, requireContext().contentResolver))
            val success = client.writeToSocket(message)

            checkDisconnected(success, message)
        }
    }

    private val registerChoosePhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = PictureManager.compressBitmap(
                PictureManager.uriToBitmap(
                    uri,
                    requireContext().contentResolver
                )
            )
            val message = ChatManager.imageMessage(clientUsername, uri.path, bitmap)
            val success = client.writeToSocket(message)

            checkDisconnected(success, message)
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
        //ChatManager.getFragmentActivity(activity)
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

        notifyAdapterChange()
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

    private fun onImageClick(path: String?, view: View) {
        val bundle: Bundle = bundleOf("path" to path)
        val extras = FragmentNavigatorExtras(view to "image_big")
        navController.navigate(
            R.id.action_chatFragment_to_imageFragment,
            bundle,
            null,
            extras
        )
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

                    sendImageButton.animate().translationX(sendImageButton.width.toFloat() * 2).apply {
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
                recordAudio.setBackgroundColor(requireContext().getColor(R.color.red))

                audioName = RecordAudioManager.nameAudio()

                recorder = RecordAudioManager.createAudioRecorder(
                    RecordAudioManager.audioDir(audioName)
                )

                recorder?.prepare()
                recorder?.start()

                disableChat(true)
            } else {
                recordAudio.setBackgroundColor(requireContext().getColor(R.color.black))

                recorder?.apply {
                    stop()
                    release()
                }

                val message = ChatManager.audioMessage(
                    clientUsername,
                    RecordAudioManager.audioDir(audioName)
                )
                val success = client.writeToSocket(message)

                checkDisconnected(success, message)

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
            val success = client.writeToSocket(message)

            disableAttention()

            checkDisconnected(success, message)
        }
    }

    private fun sendMessageListener() {
        binding.sendButton.setOnClickListener {
            if (getTextFieldString().isNotBlank()) {
                val message =
                    ChatManager.parseMessageType(clientUsername, getTextFieldString(), client.id)
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
        //chatAdapter.notifyItemChanged()
    }

    private fun takePhoto() {
        imageUri = PictureManager.createUri()
        registerTakePhoto.launch(imageUri)
    }

    private fun choosePicture() {
        registerChoosePhoto.launch("image/")
    }
}