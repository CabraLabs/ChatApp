package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.tictactoe.fragments.TictactoeFragment
import com.psandroidlabs.chatapp.tictactoe.utils.TicTacToeManager
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

    fun getFragmentActivity(parameterFragmentActivity: FragmentActivity?) {
        if (parameterFragmentActivity != null) {
            fragmentActivity = parameterFragmentActivity
        }
    }

    private fun currentTime(): String {
        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date()).uppercase()
    }

    /**
     * Determines the message type based on the beginning of the string.
     *
     * If the message starts with '/' it is probably a command.
     */
    fun determineMessageType(username: String, message: String): Message {
        return if(message.startsWith("/")) {
            when(message) {
                Constants.VIBRATE_COMMAND -> createMessage(MessageType.VIBRATE, MessageStatus.RECEIVED, username, message)
                else -> createMessage(MessageType.MESSAGE, MessageStatus.RECEIVED, username, message)
            }
        } else {
            createMessage(MessageType.MESSAGE, MessageStatus.RECEIVED, username, message)
        }
    }

    /**
     * Create a Message data class and returns it.
     */
    fun createMessage(
        type: MessageType,
        status: MessageStatus,
        username: String,
        message: String,
        date: String = currentTime()
    ) = Message(
        type,
        status,
        username,
        message,
        date
    )

    /**
     * Add the data class Message to the Chat Adapter View.
     */
    fun addToAdapter(message: Message, received: Boolean = false) {
        if (received) {
            chatList.add(message)
        } else {
            message.status = MessageStatus.SENT
            chatList.add(message)
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

    /**
     * Handles the specific message types and send them to the
     * chatList for the recycler view.
     */

    private fun startTicTacToe() {
        val ticTacToeFragment = TictactoeFragment(false)

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

        }
    }

    // TODO Make implementation somewhere for this function.
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