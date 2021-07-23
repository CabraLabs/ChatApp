package com.alexparra.chatapp.utils

import com.alexparra.chatapp.models.Server
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

    fun runServer(server: Server) {
        launch(Dispatchers.IO) {
            server.startServer()
        }
    }

    fun updateSocket(scanner: Scanner) {
        while (scanner.hasNextLine()) {
            list.add("s;${"received"};${scanner.nextLine()};${ChatManager.currentTime()}")

            withContext(Dispatchers.Main) {
                chatAdapter.notifyDataSetChanged()
            }
        }
    }
}