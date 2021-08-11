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

    inner class ViewHolderSentMessage(private val binding: ChatRowMessageSentBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceivedMessage(private val binding: ChatRowMessageReceivedBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderJoinMessage(private val binding: ChatRowJoinBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderSentVibrate(private val binding: ChatRowVibrateSentBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                //TODO set username image
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceivedVibrate(private val binding: ChatRowVibrateReceivedBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                //TODO set username image
                chatRowTime.text = message.time
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return viewTypeTranslator(dataSet[position].type, dataSet[position].status)
    }

    private fun viewTypeTranslator(type: MessageType, status: MessageStatus): Int {
        var number = 0
        if (status != MessageStatus.SENT) number = 7
        return when (type) {
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

            /** Message views **/
            0 -> {
                ViewHolderSentMessage(ChatRowMessageSentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            7 -> {
                ViewHolderReceivedMessage(ChatRowMessageReceivedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            /** Vibrate views **/
            2 -> {
                ViewHolderSentVibrate(ChatRowVibrateSentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            9 -> {
                ViewHolderReceivedVibrate(ChatRowVibrateReceivedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            /** Join view **/
            else -> {
                ViewHolderJoinMessage(ChatRowJoinBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = dataSet[position]
        viewHolder.bind(message)
    }

    override fun getItemCount() = dataSet.size
}