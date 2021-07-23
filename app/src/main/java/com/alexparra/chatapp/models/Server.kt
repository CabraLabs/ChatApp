package com.alexparra.chatapp.models

import java.net.ServerSocket
import java.net.Socket

class Server(username: String = "admin") : Chat(username) {

    private val socket = ServerSocket(1026)

    private lateinit var serverSocket: Socket

    fun startServer() {
        serverSocket = socket.accept()
    }

    fun getIpv4() {
    }

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray()
        val output = serverSocket.getOutputStream()
        output.write(messageByte)
    }

    override fun updateSocket() {
        val input = serverSocket.getInputStream()
        input.read()
    }

    override fun closeSocket() {
        serverSocket.close()
    }
}