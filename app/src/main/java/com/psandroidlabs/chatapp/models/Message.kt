package com.psandroidlabs.chatapp.models

// TODO CREATE TO STRING METHOD TO TRANSFORM INTO SENDABLE MESSAGE
data class Message(
    val type: MessageType,
    val status: MessageStatus,
    val username: String,
    val message: String,
    val time: String
)

