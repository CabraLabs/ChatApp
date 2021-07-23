package com.alexparra.chatapp.utils

import java.util.*
import kotlin.collections.ArrayList

object ChatManager {
    var chatList: ArrayList<String> = ArrayList()

    fun currentTime(): String {
        return Calendar.getInstance().time.toString()
    }
}