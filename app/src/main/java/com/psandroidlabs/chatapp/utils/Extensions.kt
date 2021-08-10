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
        "MessageType.MESSAGE" -> MessageType.MESSAGE
        "MessageType.JOIN" -> MessageType.JOIN
        "MessageType.VIBRATE" -> MessageType.VIBRATE
        "MessageType.AUDIO" -> MessageType.AUDIO
        "MessageType.IMAGE" -> MessageType.IMAGE
        "MessageType.TICINVITE" -> MessageType.TICINVITE
        "MessageType.TICPLAY" -> MessageType.TICPLAY
        else -> throw Exception("Value must be a valid MessageType")
    }
}

fun String.toMessageStatus(): MessageStatus {
    return when(this) {
        "MessageStatus.SENT" -> MessageStatus.SENT
        "MessageStatus.RECEIVED" -> MessageStatus.RECEIVED
        else -> throw Exception("Value must be a valid MessageStatus")
    }
}
