package com.alexparra.chatapp.models

import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class Server(username: String = "admin") : Chat(username) {

    private val socket = ServerSocket(1026)
    private lateinit var serverSocket: Socket
    private lateinit var output: OutputStream

    fun startServer() {
        serverSocket = socket.accept()
        output = serverSocket.getOutputStream()
    }

    override fun updateSocket(): Scanner {
        return Scanner(serverSocket.getInputStream())
    }

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray(Charsets.UTF_8)
        output.write(messageByte)
    }

    override fun closeSocket() {
        serverSocket.close()
    }
}