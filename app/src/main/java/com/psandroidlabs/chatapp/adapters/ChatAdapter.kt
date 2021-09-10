package com.psandroidlabs.chatapp.adapters

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.PictureManager
import com.psandroidlabs.chatapp.utils.RecordAudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ChatAdapter(
    private val dataSet: ArrayList<Message>, private val onImageClick: (String?, View) -> Unit
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    inner class ViewHolderMessageSent(private val binding: ChatRowMessageSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username

                if (PictureManager.loadMyAvatar() != null) {
                    userAvatar.setImageBitmap(PictureManager.loadMyAvatar())
                }

                chatRowMessage.text = message.text
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderReceivedMessage(private val binding: ChatRowMessageReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username

                if (message.id != null) {
                    val id = message.id
                    val bitmap = id?.let { PictureManager.loadMembersAvatar(it) }
                    if (bitmap != null) {
                        userAvatar.setImageBitmap(bitmap)
                    }
                }

                chatRowMessage.text = message.text
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderEventMessage(private val binding: ChatRowJoinBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                if (message.type == MessageType.JOIN.code) {
                    chatRowMessage.text = applicationContext().getString(R.string.joined_the_room)
                } else {
                    chatRowMessage.text = applicationContext().getString(R.string.left_the_room)
                }
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderVibrateSent(private val binding: ChatRowVibrateSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderReceivedVibrate(private val binding: ChatRowVibrateReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderAudioReceived(private val binding: ChatRowAudioReceivedBinding) :
        ViewHolder(binding.root) {
        private var player: MediaPlayer? = null
        private var playing: Boolean = false
        private var currentPosition = 0
        private var filePath: String = ""

        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = ChatManager.formatTime(message.time)

                if (message.id != null) {
                    val id = message.id
                    val bitmap = id?.let { PictureManager.loadMembersAvatar(it) }
                    if (bitmap != null) {
                        userAvatar.setImageBitmap(bitmap)
                    }
                }

                message.mediaId?.let {
                    filePath = RecordAudioManager.audioDir(it)
                    val length = getAudioMetadata(filePath)?.toInt()

                    if (length != null) {
                        duration.text = ChatManager.formatAudioTime(length)
                        seekBar.max = length.toInt()
                    } else {
                        playButton.visibility = View.GONE
                        seekBar.visibility = View.GONE
                        chatRowUsername.text = applicationContext().getString(R.string.audio_missing)
                        return
                    }
                }

                playButton.setOnClickListener {
                    if (!playing) {
                        playing = true

                        if (player != null) {
                            player?.start()
                        } else {
                            player = MediaPlayer().apply {
                                setDataSource(filePath)
                            }

                            player?.prepare()
                            player?.start()
                        }

                        CoroutineScope(Dispatchers.Default).launch {
                            while (player?.isPlaying == true) {
                                player?.currentPosition?.let {
                                    if (it % 1000 == 0) {
                                        withContext(Dispatchers.Main) {
                                            duration.text =
                                                ChatManager.formatAudioTime(seekBar.max - it)
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        seekBar.progress = it
                                        currentPosition = it
                                    }
                                }
                            }

                            if (playing) {
                                withContext(Dispatchers.Main) {
                                    duration.text = ChatManager.formatAudioTime(seekBar.max)
                                }

                                player?.release()
                                player = null

                                currentPosition = 0
                                seekBar.progress = 0

                                playing = false
                            }
                        }
                    } else {
                        seekBar.progress = currentPosition

                        player?.pause()
                        playing = false
                    }
                }
            }
        }

        private fun getAudioMetadata(path: String): String? {
            return try {
                val mmr = MediaMetadataRetriever().apply {
                    setDataSource(path)
                }

                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    inner class ViewHolderAudioSent(private val binding: ChatRowAudioSentBinding) :
        ViewHolder(binding.root) {
        private var player: MediaPlayer? = null
        private var playing: Boolean = false
        private var currentPosition = 0
        private var filePath: String = ""

        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = ChatManager.formatTime(message.time)

                if (PictureManager.loadMyAvatar() != null) {
                    userAvatar.setImageBitmap(PictureManager.loadMyAvatar())
                }

                message.mediaId?.let {
                    filePath = RecordAudioManager.audioDir(it)
                    val length = getAudio(filePath)?.toInt()

                    if (length != null) {
                        duration.text = ChatManager.formatAudioTime(length)
                        seekBar.max = length.toInt()
                    } else {
                        playButton.visibility = View.GONE
                        seekBar.visibility = View.GONE
                        chatRowUsername.text = applicationContext().getString(R.string.audio_missing)
                        return
                    }
                }

                playButton.setOnClickListener {
                    if (!playing) {
                        playing = true

                        if (player != null) {
                            player?.start()
                        } else {
                            player = MediaPlayer().apply {
                                setDataSource(filePath)
                            }

                            player?.prepare()
                            player?.start()
                        }

                        CoroutineScope(Dispatchers.Default).launch {
                            while (player?.isPlaying == true) {
                                player?.currentPosition?.let {
                                    if (it % 1000 == 0) {
                                        withContext(Dispatchers.Main) {
                                            duration.text =
                                                ChatManager.formatAudioTime(seekBar.max - it)
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        seekBar.progress = it
                                        currentPosition = it
                                    }
                                }
                            }

                            if (playing) {
                                withContext(Dispatchers.Main) {
                                    duration.text = ChatManager.formatAudioTime(seekBar.max)
                                }

                                player?.release()
                                player = null

                                currentPosition = 0
                                seekBar.progress = 0

                                playing = false
                            }
                        }
                    } else {
                        seekBar.progress = currentPosition

                        player?.pause()
                        playing = false
                    }
                }
            }
        }

        private fun getAudio(path: String): String? {
            return try {
                val mmr = MediaMetadataRetriever().apply {
                    setDataSource(path)
                }

                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    inner class ViewHolderImageSent(private val binding: ChatRowImageSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username

                if (PictureManager.loadMyAvatar() != null) {
                    userAvatar.setImageBitmap(PictureManager.loadMyAvatar())
                }

//                val bitmap = message.base64Data?.let { PictureManager.base64ToBitmap(it) }
//
//                if (bitmap != null) {
//                    btnChatRowImage.setImageBitmap(bitmap)
//                    btnChatRowImage.setOnClickListener {
//                        onImageClick.invoke(message.mediaId, btnChatRowImage)
//                    }
//                } else {
//                    btnChatRowImage.setImageResource(R.mipmap.ic_image_error)
//                    btnChatRowImage.setOnClickListener {
//                        onImageClick.invoke(null, btnChatRowImage)
//                    }
//                }

                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderReceivedImage(private val binding: ChatRowImageReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username

                if (message.id != null) {
                    val id = message.id
                    val bitmap = id?.let { PictureManager.loadMembersAvatar(it) }
                    if (bitmap != null) {
                        userAvatar.setImageBitmap(bitmap)
                    }
                }

                val mediaId = message.mediaId
                if (!mediaId.isNullOrBlank()) {
                    val bitmap = PictureManager.getImage(mediaId)
                    if (bitmap != null) {
                        btnChatRowImage.setImageBitmap(bitmap)

                        btnChatRowImage.setOnClickListener {
                            onImageClick.invoke(mediaId, btnChatRowImage)
                        }
                    }
                } else {
                    btnChatRowImage.setImageResource(R.mipmap.ic_image_error)
                    btnChatRowImage.setOnClickListener {
                        onImageClick.invoke(null, btnChatRowImage)
                    }
                }

                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeTranslator(dataSet[position].type, dataSet[position].status)
    }

    private fun viewTypeTranslator(type: Int, status: Int): Int {
        var number = 0
        if (status != MessageStatus.SENT.code) number = 8

        return when (type) {
            MessageType.MESSAGE.code -> MessageType.MESSAGE.code + number // 0 | 8
            MessageType.JOIN.code -> MessageType.JOIN.code + number // 1 | 9
            MessageType.VIBRATE.code -> MessageType.VIBRATE.code + number  // 2 | 10
            MessageType.AUDIO.code -> MessageType.AUDIO.code + number // 3 | 11
            MessageType.IMAGE.code -> MessageType.IMAGE.code + number // 4 | 12
            MessageType.TIC_INVITE.code -> MessageType.TIC_INVITE.code + number // 5 | 13
            MessageType.TIC_PLAY.code -> MessageType.TIC_PLAY.code + number // 6 | 14
            MessageType.LEAVE.code -> MessageType.LEAVE.code + number // 7 | 15
            else -> 666
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            /** Message Views **/
            0 -> {
                ViewHolderMessageSent(
                    ChatRowMessageSentBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            8 -> {
                ViewHolderReceivedMessage(
                    ChatRowMessageReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            /** Join Views **/
            1 -> {
                ViewHolderEventMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            9 -> {
                ViewHolderEventMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            /** Vibrate Views **/
            2 -> {
                ViewHolderVibrateSent(
                    ChatRowVibrateSentBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            10 -> {
                ViewHolderReceivedVibrate(
                    ChatRowVibrateReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            /** Audio Views **/
            3 -> {
                ViewHolderAudioSent(
                    ChatRowAudioSentBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            11 -> {
                ViewHolderAudioReceived(
                    ChatRowAudioReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            /** Image Views **/
            4 -> {
                ViewHolderImageSent(
                    ChatRowImageSentBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            12 -> {
                ViewHolderReceivedImage(
                    ChatRowImageReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            /** Leave Views **/
            7 -> {
                ViewHolderEventMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            15 -> {
                ViewHolderEventMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            /** Troll View **/
            else -> {
                TODO()
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = dataSet[position]
        viewHolder.bind(message)
    }

    override fun getItemCount() = dataSet.size
}