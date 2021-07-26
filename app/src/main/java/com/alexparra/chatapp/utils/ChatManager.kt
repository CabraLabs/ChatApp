package com.alexparra.chatapp.utils

import com.alexparra.chatapp.models.Message
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object ChatManager : CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    var chatList: ArrayList<Message> = ArrayList()

    fun currentTime(): String {

        val pattern = "HH:mm aa"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date()).uppercase()
    }
}