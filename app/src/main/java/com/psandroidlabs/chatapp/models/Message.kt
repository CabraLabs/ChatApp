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
    val partNumber: Int?,
    val dataSize: Long?,
    val dataBuffer: String?,
    var mediaId: String?,
    val time: Long,
    @Ignore var id: Int?,
    val join: Join?,
    //val ticTacToePlay: TicTacToePlay?
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

@JsonClass(generateAdapter = true)
data class TicTacToePlay(
    val isInviting: Boolean? = null,
    val isAccepting: Boolean? = null,
    val play: String? = null,
    val gameEnd: TicTacToeGameEnd,
    val opponentId: Int
)

data class Multi(val totalParts: Int, val parts: ArrayList<Part>?)
data class Part(val partNumber: Int, val base64: String?)

data class User(
    val socket: Socket,
    var profile: Profile,
)