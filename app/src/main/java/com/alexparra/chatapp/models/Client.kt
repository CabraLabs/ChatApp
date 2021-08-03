package com.alexparra.chatapp.models

import java.net.InetAddress
import java.net.Socket
import java.util.*

class ClientSocket(username: String, ip: InetAddress, port: Int) : Chat(username) {

     @Transient private val socket = Socket(ip, port)
     @Transient private val output = socket.getOutputStream()

    override fun writeToSocket(message: String) {
        val messageByte = message.toByteArray(Charsets.UTF_8)
        output.write(messageByte)
    }

    override fun readSocket(): Scanner {
        return Scanner(socket.getInputStream())
    }

    override fun closeSocket() {
        socket.close()
    }
}