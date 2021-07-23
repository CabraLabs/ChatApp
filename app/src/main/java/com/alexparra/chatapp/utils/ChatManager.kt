package com.alexparra.chatapp.utils

import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    var chatList: ArrayList<String> = ArrayList()

    fun currentTime(): String {
        return Calendar.getInstance().time.toString()
    }
}