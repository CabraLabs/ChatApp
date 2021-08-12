package com.psandroidlabs.chatapp.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    var type: Int,
    var status: Int,
    val username: String,
    val message: String,
    val time: Long,
    val ip: String,
    val join: Join
)

data class Join(val avatar: String, val password: String)