package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import java.io.IOException
import java.security.MessageDigest
import java.util.Collections.min
import kotlin.math.min


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

fun String.toSHA256(): String {
    val messageDigest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return messageDigest.fold("", { str, it -> str + "%02x".format(it) })
}

fun Bitmap.toSquare():Bitmap?{
    val side = min(width,height)

    val xOffset = (width - side) /2
    val yOffset = (height - side)/2

    return Bitmap.createBitmap(
        this,
        xOffset,
        yOffset,
        side,
        side
    )
}