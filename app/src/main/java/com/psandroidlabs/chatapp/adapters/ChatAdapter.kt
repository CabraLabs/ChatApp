package com.psandroidlabs.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.databinding.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.PictureManager


class ChatAdapter(private val dataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    inner class ViewHolderMessageSent(private val binding: ChatRowMessageSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                userAvatar.setImageBitmap(message.base64Data?.let { PictureManager.stringToBitmap(it) })
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
                userAvatar.setImageBitmap(message.base64Data?.let { PictureManager.stringToBitmap(it) })
                chatRowMessage.text = message.text
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderJoinMessage(private val binding: ChatRowJoinBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.text
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

    inner class ViewHolderImageSent(private val binding: ChatRowImageSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                userAvatar.setImageBitmap(message.base64Data?.let { PictureManager.stringToBitmap(it) })
                chatRowImage.setImageBitmap(message.text?.let { PictureManager.stringToBitmap(it) })
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    inner class ViewHolderReceivedImage(private val binding: ChatRowImageReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                userAvatar.setImageBitmap(message.base64Data?.let { PictureManager.stringToBitmap(it) })
                chatRowImage.setImageBitmap(message.text?.let { PictureManager.stringToBitmap(it) })
                chatRowTime.text = ChatManager.formatTime(message.time)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeTranslator(dataSet[position].type, dataSet[position].status)
    }

    private fun viewTypeTranslator(type: Int, status: Int): Int {
        var number = 0
        if (status != MessageStatus.SENT.code) number = 10

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
                ViewHolderJoinMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }

            9 -> {
                ViewHolderJoinMessage(
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
                TODO()
            }

            11 -> {
                TODO()
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
                TODO()
            }

            15 -> {
                TODO()
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