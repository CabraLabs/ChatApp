package com.psandroidlabs.chatapp.models

enum class UserType {
    CLIENT, SERVER
}

enum class MessageType(val code: Int) {
    MESSAGE(0),
    JOIN(1),
    VIBRATE(2),
    AUDIO(3),
    IMAGE(4),
    AUDIO_MULTIPART(5),
    IMAGE_MULTIPART(6),
    TIC_INVITE(7),
    TIC_PLAY(8),
    LEAVE(9),
    ACKNOWLEDGE(10),
    REVOKED(11),
    TIC_END(12)
}

enum class MessageStatus(val code: Int) {
    RECEIVED(0), SENT(1)
}

enum class AcceptedStatus(val code: Int) {
    ACCEPTED(0), WRONG_PASSWORD(1), SECURITY_KICK(2), ADMIN_KICK(3), MISSING_ID(4)
}

enum class TicTacToeGameEnd {
    X_WIN, O_WIN, DRAW, NONE
}