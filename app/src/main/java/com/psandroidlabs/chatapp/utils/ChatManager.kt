package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.tictactoe.fragments.TicTacToeFragment
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var fragmentActivity: FragmentActivity

    var chatList: ArrayList<Message> = ArrayList()

    private val jsonAdapter by lazy {
        val moshi: Moshi = Moshi.Builder().build()
        moshi.adapter(Message::class.java)
    }

    fun getFragmentActivity(parameterFragmentActivity: FragmentActivity?) {
        if (parameterFragmentActivity != null) {
            fragmentActivity = parameterFragmentActivity
        }
    }

    private fun formatTime(epoch: Long): String {
        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(epoch).uppercase()
    }

    private fun getEpoch(): Long {
        return Calendar.getInstance().timeInMillis
    }

    /**
     * Determines the message type based on the beginning of the string.
     *
     * If the message starts with '/' it is probably a command.
     */
    fun parseMessageType(username: String, message: String): Message {
        return if (message.startsWith("/")) {
            when (message) {
                Constants.VIBRATE_COMMAND -> createMessage(type = MessageType.VIBRATE, username = username, text = message)
                else -> createMessage(type = MessageType.MESSAGE, username = username, text = message)
            }
        } else {
            createMessage(type = MessageType.MESSAGE, username = username, text = message)
        }
    }

    /**
     * Create a Message data class and returns it.
     */
    fun createMessage(
        type: MessageType,
        status: MessageStatus = MessageStatus.RECEIVED,
        username: String,
        text: String? = null,
        base64Data: String? = null,
        date: Long = getEpoch(),
        id: Int? = null,
        avatar: String = "",
        password: String = ""
    ) = Message(
        type.code,
        status.code,
        username,
        text,
        base64Data,
        date,
        id,
        Join(avatar, password)
    )

    /**
     * Create a Message data class and returns it.
     */
    fun createMessage(
        type: MessageType,
        status: MessageStatus = MessageStatus.RECEIVED,
        username: String,
        message: String,
        date: Long = getEpoch(),
        ip: String,
        avatar: String = "",
        password: String = ""
    ) = Message(
        type.code,
        status.code,
        username,
        message,
        date,
        ip,
        Join(avatar, password)
    )

    /**
     * Add the data class Message to the Chat Adapter View.
     */
    fun addToAdapter(message: Message, received: Boolean = false) {
        if (received) {
            chatList.add(message)
            messageAction(message.type)
        } else {
            message.status = MessageStatus.SENT.code
            chatList.add(message)
        }
    }

    private fun messageAction(messageType: MessageType) {
        when (messageType) {
            MessageType.VIBRATE -> startVibrate()
            else -> return
        }
    }

    /**
     * Returns the connect message based on the user type
     */
    fun connectMessage(user: UserType, context: Context): String {
        if (user == UserType.CLIENT) {
            return context.getString(R.string.joined_the_room)
        }
        return context.getString(R.string.created_the_room)
    }

    fun serializeMessage(message: String): Message? {
        return jsonAdapter.fromJson(message)
    }

    fun parseToJson(message: Message): String {
        return jsonAdapter.toJson(message)
    }

    private fun startTicTacToe() {
        val ticTacToeFragment = TicTacToeFragment(false)

        fragmentActivity.supportFragmentManager.let {
            ticTacToeFragment.show(it, null)
        }
    }

    private fun ticTacToeListener() {
        fragmentActivity.let {
            Snackbar.make(
                it.findViewById(R.id.chatLayout),
                applicationContext().getString(R.string.accept),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("YES") { startTicTacToe() }.show()
        }
    }

    private fun startVibrate() {
        val vibrator =
            ContextCompat.getSystemService(applicationContext(), Vibrator::class.java) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    1000,
                    VibrationEffect.EFFECT_HEAVY_CLICK
                )
            )
        } else {
            toast("*vibrating*")
        }
    }

    fun delay(delay: Long = 1500, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }
    
    fun playSound() {
        ContextCompat.getSystemService(applicationContext(), AudioManager::class.java)?.apply {
            setStreamVolume(AudioManager.STREAM_MUSIC, getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.AUDIOFOCUS_GAIN)
        }
        val mediaPlayer = MediaPlayer.create(applicationContext(), R.raw.goat)
        mediaPlayer.start()
    }
}