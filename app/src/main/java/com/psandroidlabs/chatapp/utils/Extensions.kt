package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.models.MessageStatus
import com.psandroidlabs.chatapp.models.MessageType
import java.lang.Exception


fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_LONG) {
    context?.let {
        Toast.makeText(it, text, duration).show()
    } ?: Toast.makeText(applicationContext(), text, duration).show()
}

fun Any.toast(text: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(applicationContext(), text, duration).show()
}

fun Fragment.hideKeyboard() {
    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    inputMethodManager.apply {
        hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
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
