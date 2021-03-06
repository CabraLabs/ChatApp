package com.psandroidlabs.chatapp.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.models.*
import com.psandroidlabs.chatapp.models.Message
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Default

    var chatList: ArrayList<Message> = ArrayList()
    var chatMembersList: ArrayList<Profile> = ArrayList()

    val multiPart: HashMap<Int?, Multi?> = hashMapOf()
    val hashMapMutex = Mutex()

    private val jsonAdapter by lazy {
        val moshi: Moshi = Moshi.Builder().build()
        moshi.adapter(Message::class.java)
    }

    private val jsonProfileAdapter by lazy {
        val listAdapter = Types.newParameterizedType(List::class.java, Profile::class.java)
        val adapter: JsonAdapter<List<Profile>> =
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listAdapter)
        adapter
    }

    fun formatTime(epoch: Long): String {
        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.US)
        return simpleDateFormat.format(epoch).uppercase()
    }

    fun formatAudioTime(time: Int): String {
        val pattern = "mm:ss"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.US)
        return simpleDateFormat.format(time)
    }

    fun getEpoch(): Long {
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
                Constants.VIBRATE_COMMAND -> createMessage(
                    type = MessageType.VIBRATE,
                    username = username,
                    text = message,
                    id = id
                )
                else -> {
                    playSound(R.raw.zap)
                    createMessage(
                        type = MessageType.MESSAGE,
                        username = username,
                        text = "sifu 8==D-- ( . Y . )",
                        id = id
                    )
                }
            }
        } else {
            createMessage(type = MessageType.MESSAGE, username = username, text = message, id = id)
        }
    }

    /**
     * Message creator that makes easy to abstract specific message types.
     */
    private fun createMessage(
        type: MessageType,
        status: MessageStatus = MessageStatus.RECEIVED,
        username: String? = null,
        text: String? = null,
        partNumber: Int? = null,
        dataSize: Long? = null,
        dataBuffer: String? = null,
        mediaId: String? = null,
        date: Long? = null,
        id: Int? = null,
        avatar: String? = null,
        password: String? = null,
        isAdmin: Boolean? = null
    ) = Message(
        type = type.code,
        status = status.code,
        username = username,
        text = text,
        partNumber = partNumber,
        dataSize = dataSize,
        dataBuffer = dataBuffer,
        mediaId = mediaId,
        time = date ?: getEpoch(),
        id = id,
        join = Join(avatar, password, isAdmin)
    )

    /**
     * Creates an audio message
     */
    private fun audioMessage(
        type: MessageType,
        username: String,
        audioName: String?,
        byteBuffer: String? = null,
        partNumber: Int? = null,
        dataSize: Long? = null,
        date: Long? = null
    ): Message {
        audioName?.let {
            return createMessage(
                type = type,
                username = username,
                partNumber = partNumber,
                dataSize = dataSize,
                dataBuffer = byteBuffer,
                mediaId = audioName,
                date = date
            )
        } ?: throw Exception("Audio message needs to have a full path.")
    }

    /**
     * Creates a buffered audio message which returns an arraylist of messages with all
     * the audio parts.
     */
    fun bufferedAudioMessage(username: String, audioName: String): Pair<Message, ArrayList<Message>> {
        val messageList = arrayListOf<Message>()
        var part = 0

        val path = RecordAudioManager.audioDir(audioName)
        val base64 = RecordAudioManager.audioBase64(path)
        var size = base64.length

        while (size > 0) {
            val actualSize = part * Constants.PART_BUFFER_SIZE
            if (size >= Constants.PART_BUFFER_SIZE) {
                val data = base64.slice(actualSize until (actualSize + Constants.PART_BUFFER_SIZE))
                messageList.add(
                    audioMessage(
                        type = MessageType.AUDIO_MULTIPART,
                        username = username,
                        audioName = audioName,
                        byteBuffer = data,
                        partNumber = part,
                        dataSize = if (part == 0) size.toLong() else null,
                        date = getEpoch(),
                    )
                )
            } else {
                val data = base64.slice(actualSize until (actualSize + size))
                messageList.add(
                    audioMessage(
                        type = MessageType.AUDIO_MULTIPART,
                        username = username,
                        audioName = audioName,
                        byteBuffer = data,
                        partNumber = part,
                        dataSize = if (part == 1) size.toLong() else null,
                        date = getEpoch(),
                    )
                )
            }

            part++
            size -= Constants.PART_BUFFER_SIZE
        }

        val fullMessage = audioMessage(MessageType.AUDIO, username, audioName)
        return Pair(fullMessage, messageList)
    }

    fun bufferedImageMessage(username: String, imageName: String): Pair<Message, ArrayList<Message>> {
        val messageList = arrayListOf<Message>()
        var part = 0

        val base64 = PictureManager.getImage(imageName)?.toBase64() ?: ""
        var size = base64.length

        while (size > 0) {
            val actualSize = part * Constants.PART_BUFFER_SIZE
            if (size >= Constants.PART_BUFFER_SIZE) {
                val data = base64.slice(actualSize until (actualSize + Constants.PART_BUFFER_SIZE))
                messageList.add(
                    imageMessage(
                        type = MessageType.IMAGE_MULTIPART,
                        username = username,
                        imageName = imageName,
                        byteBuffer = data,
                        partNumber = part,
                        dataSize = if (part == 0) size.toLong() else null,
                        date = getEpoch(),
                    )
                )
            } else {
                val data = base64.slice(actualSize until (actualSize + size))
                messageList.add(
                    imageMessage(
                        type = MessageType.IMAGE_MULTIPART,
                        username = username,
                        imageName = imageName,
                        byteBuffer = data,
                        partNumber = part,
                        dataSize = if (part == 1) size.toLong() else null,
                        date = getEpoch(),
                    )
                )
            }

            part++
            size -= Constants.PART_BUFFER_SIZE
        }

        val fullMessage = imageMessage(MessageType.IMAGE, username, imageName)
        return Pair(fullMessage, messageList)
    }

    fun deductTotalParts(size: Long?): Int {
        if (size != null) {
            return (size / Constants.PART_BUFFER_SIZE).toInt() + 1
        }
        return 0
    }

    suspend fun createAudio(base64: String, username: String?): Message = withContext(Dispatchers.Default) {
        val audioName = RecordAudioManager.base64toAudio(base64)
        if (username != null) {
            return@withContext audioMessage(MessageType.AUDIO, username, audioName)
        }

        throw Exception("Message needs to provide an username.")
    }

    fun createImage(base64: String, username: String?): Message {
        val bitmap = base64.let { PictureManager.base64ToBitmap(it) }
        val imageName = PictureManager.setImageName()
        if (bitmap != null) {
            PictureManager.bitmapToUri(bitmap, imageName)
        }

        if (username != null) {
            return imageMessage(MessageType.IMAGE, username, imageName)
        }

        throw Exception("Message needs to provide an username.")
    }

    /**
     * Create a proper authorization connect message to the server.
     */
    fun connectMessage(username: String, text: String, password: String? = null) = createMessage(
        type = MessageType.JOIN,
        username = username,
        text = text,
        password = password,
        avatar = PictureManager.loadMyAvatar()?.toBase64()
    )

    /**
     * Leave message to properly inform users of a disconnection.
     */
    fun leaveMessage(username: String) = createMessage(
        type = MessageType.LEAVE,
        username = username,
        text = applicationContext().getString(R.string.left_the_room)
    )

    /**
     * Vibrate message creator for the vibrate button.
     */
    fun vibrateMessage(username: String, id: Int) = createMessage(
        type = MessageType.VIBRATE,
        username = username,
        id = id
    )

    private fun imageMessage(
        type: MessageType,
        username: String,
        imageName: String?,
        byteBuffer: String? = null,
        partNumber: Int? = null,
        dataSize: Long? = null,
        date: Long? = null
    ): Message {
        imageName?.let {
            return createMessage(
                type = type,
                username = username,
                partNumber = partNumber,
                dataSize = dataSize,
                dataBuffer = byteBuffer,
                mediaId = imageName,
                date = date
            )
        } ?: throw Exception("Image message needs to have a full path.")
    }

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
            message.status = MessageStatus.RECEIVED.code
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

    fun serializeProfiles(profileList: String): List<Profile>? {
        return jsonProfileAdapter.fromJson(profileList)
    }

//    private fun startTicTacToe() {
//        val ticTacToeFragment = TicTacToeFragment(false)
//
//        fragmentActivity.supportFragmentManager.let {
//            ticTacToeFragment.show(it, null)
//        }
//    }
//
//    private fun ticTacToeListener() {
//        fragmentActivity.let {
//            Snackbar.make(
//                it.findViewById(R.id.chatLayout),
//                applicationContext().getString(R.string.accept_tictactoe_invite),
//                Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction("YES") { startTicTacToe() }.show()
//        }
//    }

    fun startVibrate() {
        val vibrator =
            ContextCompat.getSystemService(applicationContext(), Vibrator::class.java) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    1000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(1000)
        }
    }

    fun playSound(audioId: Int = R.raw.goat) {
        ContextCompat.getSystemService(applicationContext(), AudioManager::class.java)?.apply {
            setStreamVolume(
                AudioManager.STREAM_MUSIC,
                getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        val mediaPlayer = MediaPlayer.create(applicationContext(), audioId)
        mediaPlayer.start()
    }

    fun delay(delay: Long = 1500, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(action, delay)
    }

    fun requestPermission(activity: Activity?, permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(
                applicationContext(), permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            if (activity != null) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            }
            return false
        }
        return true
    }
}