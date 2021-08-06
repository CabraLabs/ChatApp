package com.psandroidlabs.chatapp.utils

import android.widget.Toast
import androidx.fragment.app.Fragment


fun Fragment.toast(text: String, duration: Int = Toast.LENGTH_LONG) {
    context?.let {
        Toast.makeText(it, text, duration).show()
    }
}

