package com.psandroidlabs.chatapp.tictactoe

data class TicTacToeGame(val socketId1: Int, val socketId2: Int) {
    val board = arrayListOf<String>()
    // true = X | false = O
    val round = true
}
