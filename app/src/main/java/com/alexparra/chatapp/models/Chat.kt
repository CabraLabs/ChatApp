package com.alexparra.chatapp.models

import java.io.Serializable

interface Chat : Serializable {
    fun writeToSocket(message: ByteArray)
    fun updateSocket()
    fun closeSocket()
}