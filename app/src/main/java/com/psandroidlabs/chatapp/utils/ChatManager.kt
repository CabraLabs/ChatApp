package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
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

    fun formatTime(epoch: Long): String {
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
        username: String? = null,
        text: String? = null,
        base64Data: String? = null,
        date: Long = getEpoch(),
        id: Int? = null,
        avatar: String? = null,
        password: String? = null
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
     * Create a proper authorization connect message to the server.
     */
    fun connectMessage(username: String, text: String, password: String? = null) = createMessage(
        type = MessageType.JOIN,
        status = MessageStatus.RECEIVED,
        username = username,
        text = text,
        password = password
    )

    /**
     * Parse the message to a valid REVOKED or ACKNOWLEDGE message.
     */
    fun parseAcceptMessage(status: AcceptedStatus, id: Int): Message {
        return if (status != AcceptedStatus.ACCEPTED) {
            createMessage(
                MessageType.REVOKED, MessageStatus.RECEIVED, id = id
            )
        } else {
            createMessage(
                MessageType.ACKNOWLEDGE, MessageStatus.RECEIVED, id = id
            )
        }
    }

    /**
     * Add the data class Message to the Chat Adapter View.
     */
    fun addToAdapter(message: Message, received: Boolean = false) {
        if (received) {
            chatList.add(message)
        } else {
            message.status = MessageStatus.SENT.code
            chatList.add(message)
        }
    }

    fun serializeMessage(message: String): Message? {
        return jsonAdapter.fromJson(message)
    }

    fun parseToJson(message: Message): String {
        return jsonAdapter.toJson(message) + "\n"
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
        val vibrator = ContextCompat.getSystemService(applicationContext(), Vibrator::class.java) as Vibrator
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

    fun playSound() {
        ContextCompat.getSystemService(applicationContext(), AudioManager::class.java)?.apply {
            setStreamVolume(AudioManager.STREAM_MUSIC, getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.AUDIOFOCUS_GAIN)
        }
        val mediaPlayer = MediaPlayer.create(applicationContext(), R.raw.goat)
        mediaPlayer.start()
    }

    fun delay(delay: Long = 1500, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }
}