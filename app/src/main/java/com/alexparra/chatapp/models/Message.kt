package com.alexparra.chatapp.models

data class Message(
    val type: MessageType,
    val username: String,
    val message: String,
    val time: String
)

enum class MessageType {
    SENT, RECEIVED, JOINED, VIBRATE
}