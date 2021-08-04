package com.alexparra.chatapp.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.alexparra.chatapp.MainApplication.Companion.applicationContext
import com.alexparra.chatapp.R
import com.alexparra.chatapp.models.Message
import java.text.SimpleDateFormat
import com.alexparra.chatapp.models.MessageType
import com.alexparra.chatapp.models.UserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*
import kotlin.collections.ArrayList


object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    var chatList: ArrayList<Message> = ArrayList()

    private fun currentTime(): String {
        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date()).uppercase()
    }

    fun sendVibrateMessage(username: String) {
        chatList.add(Message(MessageType.ATTENTION, username, "/vibrate", currentTime()))
    }

    /**
     * Sends the Message data class when the user joins the chat.
     */
    fun connectMessage(user: UserType, username: String,context: Context): Message {
        if (user == UserType.CLIENT) {
            return Message(MessageType.JOINED, username, context.getString(R.string.joined_the_room), currentTime())
        }
        return Message(MessageType.JOINED, username, context.getString(R.string.created_the_room), currentTime())
    }

    /**
     * Send the correct formatted message output to the Socket.
     */
    fun sendMessageToSocket(username: String, text: String): String {
        return "${username};${text};${currentTime()};\n"
    }

    /**
     * Generates the Message data class for the sent message.
     */
    fun getSentMessage(username: String, text: String): Message {
        return Message(MessageType.SENT, username, text, currentTime())
    }

    /**
     * Handles the specific message types and send them to the
     * chatList for the recycler view.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun updateRecyclerMessages(message: List<String>) {
        when {
            message[1] == "/vibrate" -> {
                startVibrate()
                chatList.add(
                    Message(
                        MessageType.ATTENTION,
                        message[0],
                        message[1],
                        message[2]
                    )
                )
            }

            message[3].isNotBlank() -> {
                chatList.add(
                    Message(
                        MessageType.JOINED,
                        message[0],
                        message[1],
                        message[2]
                    )
                )
            }

            else -> {
                chatList.add(
                    Message(
                        MessageType.RECEIVED,
                        message[0],
                        message[1],
                        message[2]
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startVibrate() {
        val vibrator = ContextCompat.getSystemService(applicationContext(), Vibrator::class.java)
        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                1000,
                VibrationEffect.EFFECT_HEAVY_CLICK
            )
        )
    }

    fun playSound() {
        ContextCompat.getSystemService(applicationContext(), AudioManager::class.java)?.apply {
            setStreamVolume(AudioManager.STREAM_MUSIC, getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.AUDIOFOCUS_GAIN)
        }
        val mediaPlayer = MediaPlayer.create(applicationContext(), R.raw.bolso)
        mediaPlayer.start()
    }

    fun delay(delay: Long = 1500, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }
}