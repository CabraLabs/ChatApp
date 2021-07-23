package com.alexparra.chatapp.models

import java.io.Serializable

abstract class Chat(username: String) : Serializable {
    abstract fun writeToSocket(message: ByteArray)
    abstract fun updateSocket()
    abstract fun closeSocket()
}