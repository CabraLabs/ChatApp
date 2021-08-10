package com.psandroidlabs.chatapp.models

import com.psandroidlabs.chatapp.utils.toMessageStatus
import com.psandroidlabs.chatapp.utils.toMessageType

data class Message(
    var type: MessageType,
    var status: MessageStatus,
    val username: String,
    val message: String,
    val time: String
) {
    override fun toString(): String {
        return "${this.type};${this.status};${this.username};${this.message};${this.time}"
    }

    constructor(messageList: List<String>) : this(
        type = messageList[0].toMessageType(),
        status = messageList[1].toMessageStatus(),
        username = messageList[2],
        message = messageList[3],
        time = messageList[4]
    )
}