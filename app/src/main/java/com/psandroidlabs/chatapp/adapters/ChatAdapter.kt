package com.psandroidlabs.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.databinding.ChatRowItemEventBinding
import com.psandroidlabs.chatapp.databinding.ChatRowItemReceiveBinding
import com.psandroidlabs.chatapp.databinding.ChatRowItemSendBinding
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageType


class ChatAdapter(private val dataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(message: Message)
    }

    inner class ViewHolderSendMessage(private val binding: ChatRowItemSendBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderReceiveMessage(private val binding: ChatRowItemReceiveBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    inner class ViewHolderEventMessage(private val binding: ChatRowItemEventBinding) : ViewHolder(binding.root) {
        override fun bind(message: Message) {
            with(binding) {
                chatRowUsername.text = message.username
                chatRowMessage.text = message.message
                chatRowTime.text = message.time
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataSet[position].type) {
            MessageType.SENT -> 1
            MessageType.RECEIVED -> 2
            else -> 3
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            1 -> {
                ViewHolderSendMessage(ChatRowItemSendBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            2 -> {
                ViewHolderReceiveMessage(ChatRowItemReceiveBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }

            else -> {
                ViewHolderEventMessage(ChatRowItemEventBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val message = dataSet[position]
        viewHolder.bind(message)
    }

    override fun getItemCount() = dataSet.size
}