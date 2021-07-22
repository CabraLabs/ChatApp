package com.alexparra.chatapp.models

import java.net.InetAddress
import java.net.Socket

class ClientSocket(val username: String, ip: InetAddress, port: Int) : Chat {

    private val socket = Socket(ip, port)

    override fun writeToSocket(message: ByteArray) {
        val output = socket.getOutputStream()
        output.write(message)
    }

    override fun updateSocket() {
        val input = socket.getInputStream()
        input.read()
    }

    override fun closeSocket() {
        socket.close()
    }
}