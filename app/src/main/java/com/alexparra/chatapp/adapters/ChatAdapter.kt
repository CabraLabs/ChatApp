package com.alexparra.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
<<<<<<< Updated upstream:app/src/main/java/com/alexparra/chatapp/adapters/ChatAdapter.kt
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.ChatRowItemEventBinding
import com.alexparra.chatapp.databinding.ChatRowItemReceiveBinding
import com.alexparra.chatapp.databinding.ChatRowItemSendBinding
import com.alexparra.chatapp.databinding.ChatRowItemVibrateBinding
import com.alexparra.chatapp.models.Message
import com.alexparra.chatapp.models.MessageType
=======
import com.psandroidlabs.chatapp.databinding.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
>>>>>>> Stashed changes:app/src/main/java/com/psandroidlabs/chatapp/adapters/ChatAdapter.kt

class ChatAdapter(private val dataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    inner class ViewHolderSendMessage(private val binding: ChatRowMessageSentBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceiveMessage(private val binding: ChatRowMessageReceivedBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderEventMessage(private val binding: ChatRowJoinBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

<<<<<<< Updated upstream:app/src/main/java/com/alexparra/chatapp/adapters/ChatAdapter.kt
    inner class ViewHolderVibrateMessage(private val binding: ChatRowItemVibrateBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
                chatRowImage.setImageResource(R.drawable.ic_vibrate)
=======
    inner class ViewHolderReceivedVibrate(private val binding: ChatRowVibrateReceivedBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderSentVibrate(private val binding: ChatRowVibrateSentBinding): ViewHolder(binding.root){
        override fun bind(message: Message) {
            with(binding){
                chatRowUsername.text = message.username
>>>>>>> Stashed changes:app/src/main/java/com/psandroidlabs/chatapp/adapters/ChatAdapter.kt
                chatRowTime.text = message.time
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
<<<<<<< Updated upstream:app/src/main/java/com/alexparra/chatapp/adapters/ChatAdapter.kt
        return when (dataSet[position].type){
            MessageType.SENT -> 1
            MessageType.RECEIVED -> 2
            MessageType.JOINED -> 3
            else -> 3
=======
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
>>>>>>> Stashed changes:app/src/main/java/com/psandroidlabs/chatapp/adapters/ChatAdapter.kt
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType){
            0 -> {
                ViewHolderSendMessage(ChatRowMessageSentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            2 -> {
                ViewHolderSentVibrate(ChatRowVibrateSentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            7 -> {
                ViewHolderReceiveMessage(ChatRowMessageReceivedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            9 -> {
                ViewHolderReceivedVibrate(ChatRowVibrateReceivedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

<<<<<<< Updated upstream:app/src/main/java/com/alexparra/chatapp/adapters/ChatAdapter.kt
            3 -> {
                ViewHolderEventMessage(ChatRowItemEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
=======
            else -> {
                ViewHolderEventMessage(ChatRowJoinBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
>>>>>>> Stashed changes:app/src/main/java/com/psandroidlabs/chatapp/adapters/ChatAdapter.kt
            }

            else -> {
                ViewHolderVibrateMessage(ChatRowItemVibrateBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val message = dataSet[position]
        viewHolder.bind(message)
    }

    override fun getItemCount() = dataSet.size
}