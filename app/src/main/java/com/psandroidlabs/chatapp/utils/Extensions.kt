package com.psandroidlabs.chatapp.utils

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
import java.lang.Exception


fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_LONG) {
    context?.let {
        Toast.makeText(it, text, duration).show()
    }
}

fun String.toMessageType(): MessageType {
    return when(this) {
        "MESSAGE" -> MessageType.MESSAGE
        "JOIN" -> MessageType.JOIN
        "VIBRATE" -> MessageType.VIBRATE
        "AUDIO" -> MessageType.AUDIO
        "IMAGE" -> MessageType.IMAGE
        "TICINVITE" -> MessageType.TICINVITE
        "TICPLAY" -> MessageType.TICPLAY
        else -> throw Exception("Value must be a valid MessageType")
    }
}

fun String.toMessageStatus(): MessageStatus {
    return when(this) {
        "SENT" -> MessageStatus.SENT
        "RECEIVED" -> MessageStatus.RECEIVED
        else -> throw Exception("Value must be a valid MessageStatus")
    }
}
