package com.psandroidlabs.chatapp.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.net.Socket

@Entity
@JsonClass(generateAdapter = true)
data class Message(
    @PrimaryKey(autoGenerate = true) val dbId: Int? = null,
    var type: Int,
    var status: Int,
    val username: String?,
    val text: String?,
    @Ignore var base64Data: String?,
    var mediaId: String?,
    val time: Long,
    @Ignore var id: Int?,
    val join: Join?
)

@JsonClass(generateAdapter = true)
data class Join(val avatar: String?, val password: String?, var isAdmin: Boolean?)

@JsonClass(generateAdapter = true)
data class Profile(
    var id: Int? = null,
    var name: String? = null,
    var photoProfile: String? = null,
    var scoreTicTacToe: Int? = null
)

data class User(
    val socket: Socket,
    var profile: Profile,
)