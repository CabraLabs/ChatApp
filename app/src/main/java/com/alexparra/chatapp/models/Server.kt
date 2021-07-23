package com.alexparra.chatapp.models

import java.net.ServerSocket
import java.net.Socket
import java.util.*

class Server(username: String = "admin") : Chat(username) {

    private val socket = ServerSocket(1026)
    private lateinit var serverSocket: Socket

    fun startServer() {
        serverSocket = socket.accept()
    }

    override fun updateSocket(): Scanner {
        return Scanner(serverSocket.getInputStream())
    }

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray()
        val output = serverSocket.getOutputStream()
        output.write(messageByte)
    }



    override fun closeSocket() {
        serverSocket.close()
    }
}