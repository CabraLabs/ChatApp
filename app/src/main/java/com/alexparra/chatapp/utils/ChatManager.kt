package com.alexparra.chatapp.utils

import android.content.Context
import com.alexparra.chatapp.R
import com.alexparra.chatapp.models.Chat
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.models.Message
import java.text.SimpleDateFormat
import com.alexparra.chatapp.models.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.collections.ArrayList

object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    var chatList: ArrayList<Message> = ArrayList()

    fun currentTime(): String {
        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date()).uppercase()
    }

    /**
     * Sends the Message data class when the user joins the chat.
     */
    fun connectMessage(chat: Chat, context: Context): Message {
        if (chat is ClientSocket) {
            return Message(MessageType.JOINED, chat.username, context.getString(R.string.joined_the_room), currentTime())
        }
        return Message(MessageType.JOINED, chat.username, context.getString(R.string.created_the_room), currentTime())
    }

    /**
     * Send the correct formatted message output to the Socket.
     */
    fun sendMessageToSocket(chat: Chat, text: String): String {
        return "${chat.username};${text};${currentTime()}\n"
    }

    /**
     * Generates the Message data class for the sent message.
     */
    fun getSentMessage(chat: Chat, text: String): Message {
        return Message(MessageType.SENT, chat.username, text, currentTime())
    }
}