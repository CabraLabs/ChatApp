package com.alexparra.chatapp.models

import java.io.Serializable
import java.util.*

abstract class Chat(val username: String) : Serializable {
    abstract fun writeToSocket(message: String)
    abstract fun readSocket(): Scanner
    //abstract fun sendImage()
    abstract fun closeSocket()
}