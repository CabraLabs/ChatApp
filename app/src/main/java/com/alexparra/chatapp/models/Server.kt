package com.alexparra.chatapp.models

import java.net.ServerSocket
import java.net.Socket

class Server(username: String = "admin") : Chat(username) {

    private val socket = ServerSocket(1026)

    private lateinit var serverSocket: Socket

    fun startServer() {
        serverSocket = socket.accept()
    }

    override fun writeToSocket(message: ByteArray) {
        val output = serverSocket.getOutputStream()
        output.write(message)
    }

    override fun updateSocket() {
        val input = serverSocket.getInputStream()
        input.read()
    }

    override fun closeSocket() {
        serverSocket.close()
    }
}