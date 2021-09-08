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
    TIC_INVITE(5),
    TIC_PLAY(6),
    LEAVE(7),
    ACKNOWLEDGE(8),
    REVOKED(9)
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