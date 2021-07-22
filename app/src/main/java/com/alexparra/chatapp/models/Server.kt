package com.alexparra.chatapp.models

import java.net.ServerSocket

class Server : Chat {

    private val serverSocket = ServerSocket(13).accept()

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