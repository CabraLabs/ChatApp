package com.psandroidlabs.chatapp.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    var type: Int,
    var status: Int,
    val username: String?,
    val text: String?,
    val base64Data: String?,
    val time: Long,
    var id: Int?,
    val join: Join?
)

@JsonClass(generateAdapter = true)
data class Join(val avatar: String?, val password: String?)