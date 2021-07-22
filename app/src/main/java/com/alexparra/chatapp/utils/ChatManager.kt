package com.alexparra.chatapp.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

object ChatManager {
    var chatList: ArrayList<String> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    fun currentTime(): String{
        return LocalDateTime.now().toString()
    }
}