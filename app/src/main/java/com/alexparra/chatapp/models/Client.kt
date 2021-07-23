package com.alexparra.chatapp.models

import java.net.InetAddress
import java.net.Socket

class ClientSocket(username: String, ip: InetAddress, port: Int) : Chat(username) {

    private val socket = Socket(ip, port)

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray(Charsets.US_ASCII)
        val output = socket.getOutputStream()
        output.write(messageByte)
    }

    override fun updateSocket() {
        val input = socket.getInputStream()
        input.read()
    }

    override fun closeSocket() {
        socket.close()
    }
}