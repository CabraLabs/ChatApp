package com.psandroidlabs.chatapp.utils

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.tictactoe.fragments.TicTacToeFragment
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
    var chatMembersList: ArrayList<Profile> = ArrayList()

    private val jsonAdapter by lazy {
        val moshi: Moshi = Moshi.Builder().build()
        moshi.adapter(Message::class.java).lenient()
    }

    private val jsonProfileAdapter by lazy {
        val listAdapter = Types.newParameterizedType(List::class.java, Profile::class.java)
        val adapter: JsonAdapter<List<Profile>> = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listAdapter)
        adapter
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
    fun parseMessageType(username: String, message: String, id: Int): Message {
        return if (message.startsWith("/")) {
            when (message) {
                Constants.VIBRATE_COMMAND -> createMessage(type = MessageType.VIBRATE, username = username, text = message, id= id)
                else -> createMessage(type = MessageType.MESSAGE, username = username, text = message, id = id)
            }
        } else {
            createMessage(type = MessageType.MESSAGE, username = username, text = message, id = id)
        }
    }

    /**
     * Create a Message data class and returns it.
     */
    private fun createMessage(
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
        type = type.code,
        status = status.code,
        username = username,
        text = text,
        base64Data = base64Data,
        time = date,
        id = id,
        join = Join(avatar, password)
    )

    /**
     * Create a proper authorization connect message to the server.
     */
    fun connectMessage(username: String, text: String, password: String? = null) = createMessage(
        type = MessageType.JOIN,
        username = username,
        text = text,
        password = password
    )

    /**
     * Vibrate message creator for the vibrate button.
     */
    fun vibrateMessage(username: String, id: Int) = createMessage(
        type = MessageType.VIBRATE,
        username = username,
        id = id
    )

    /**
     * Parse the message to a valid REVOKED or ACKNOWLEDGE message.
     */
    fun parseAcceptMessage(status: AcceptedStatus, id: Int, profileList: String? = null): Message {
        return if (status != AcceptedStatus.ACCEPTED) {
            createMessage(
                MessageType.REVOKED, id = id
            )
        } else {
            createMessage(
                MessageType.ACKNOWLEDGE, id = id, text = profileList
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

    fun parseProfileList(profileList: List<Profile>): String {
        return jsonProfileAdapter.toJson(profileList)
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
                applicationContext().getString(R.string.accept_tictactoe_invite),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("YES") { startTicTacToe() }.show()
        }
    }

    fun startVibrate() {
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

    fun scrollChat(recyclerView: RecyclerView) {
        recyclerView.scrollToPosition(chatList.size - 1)
    }

    fun parseProfile(profileList: String): Profile{
        TODO()
    }
}