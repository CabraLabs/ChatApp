package com.psandroidlabs.chatapp.models

import com.squareup.moshi.JsonClass
import java.net.Socket

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

@JsonClass(generateAdapter = true)
data class Profile(
    var id : Int?,
    var name : String,
    var photoProfile : String?,
    var scoreTicTacToe: Int?
)

data class User(
    val socket: Socket,
    var profile: Profile?,
)