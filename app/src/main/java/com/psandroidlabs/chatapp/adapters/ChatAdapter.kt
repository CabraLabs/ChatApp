package com.psandroidlabs.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.databinding.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType

class ChatAdapter(private val dataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    inner class ViewHolderSendMessage(private val binding: ChatRowMessageSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceiveMessage(private val binding: ChatRowMessageReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderEventMessage(private val binding: ChatRowJoinBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceivedVibrate(private val binding: ChatRowVibrateReceivedBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderSentVibrate(private val binding: ChatRowVibrateSentBinding) :
        ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowTime.text = message.time
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var number = 0
        if (dataSet[position].status != MessageStatus.SENT) number = 7
        return when (dataSet[position].type) {
            MessageType.MESSAGE -> MessageType.MESSAGE.code + number
            MessageType.JOIN -> MessageType.JOIN.code + number
            MessageType.VIBRATE -> MessageType.VIBRATE.code + number
            MessageType.AUDIO -> MessageType.AUDIO.code + number
            MessageType.IMAGE -> MessageType.IMAGE.code + number
            MessageType.TICINVITE -> MessageType.TICINVITE.code + number
            MessageType.TICPLAY -> MessageType.TICPLAY.code + number
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> {
                ViewHolderSendMessage(
                    ChatRowMessageSentBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            2 -> {
                ViewHolderSentVibrate(
                    ChatRowVibrateSentBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            7 -> {
                ViewHolderReceiveMessage(
                    ChatRowMessageReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            9 -> {
                ViewHolderReceivedVibrate(
                    ChatRowVibrateReceivedBinding.inflate(
                        LayoutInflater.from(
                            viewGroup.context
                        ), viewGroup, false
                    )
                )
            }

            else -> {
                ViewHolderEventMessage(
                    ChatRowJoinBinding.inflate(
                        LayoutInflater.from(viewGroup.context),
                        viewGroup,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = dataSet[position]
        viewHolder.bind(message)
    }

    override fun getItemCount() = dataSet.size
}