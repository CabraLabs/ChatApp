package com.alexparra.chatapp.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Serializable
import java.net.Socket

interface Chat : Serializable {
    fun writeToSocket(message: ByteArray)
    fun updateSocket()
}

class ClientSocket(val username: String, ip: String, port: Int) : Chat, CoroutineScope {
    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    private val socket = Socket(ip, port)

    override fun writeToSocket(message: ByteArray) {
        val output = socket.getOutputStream()
        launch {
            output.write(message)
        }
    }

    override fun updateSocket() {
        val input = socket.getInputStream()
        launch {
            input.read()
        }
    }
}