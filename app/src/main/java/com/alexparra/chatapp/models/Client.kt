package com.alexparra.chatapp.models

import java.net.InetAddress
import java.net.Socket
import java.util.*

class ClientSocket(username: String, ip: InetAddress, port: Int) : Chat(username) {

    private val socket = Socket(ip, port)
    private val output = socket.getOutputStream()

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray(Charsets.US_ASCII)
        output.write(messageByte)
    }

    override fun updateSocket(): Scanner {
        return Scanner(socket.getInputStream())
    }

    override fun closeSocket() {
        socket.close()
    }
}