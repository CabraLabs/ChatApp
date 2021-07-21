package com.alexparra.chatapp.models

import java.net.Socket

class Client(host: String, port: Int) {

    val socket = Socket(host, port)
    val output = socket.getOutputStream()
    val input = socket.getInputStream()

    fun writeToSocket(message: ByteArray) {
        output.write(message)
    }

    fun updateSocket() {
        input.read()
    }
}