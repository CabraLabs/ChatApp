package com.psandroidlabs.chatapp.models

enum class UserType {
    CLIENT, SERVER
}

enum class MessageType(val code: Int) {
    MESSAGE(0), JOIN(1), VIBRATE(2), AUDIO(3), IMAGE(4), TICINVITE(5), TICPLAY(6)
}

enum class MessageStatus {
    RECEIVED, SENT
}