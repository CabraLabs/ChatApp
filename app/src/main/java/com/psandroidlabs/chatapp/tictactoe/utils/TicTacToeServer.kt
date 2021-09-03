package com.psandroidlabs.chatapp.tictactoe.utils

import com.psandroidlabs.chatapp.models.Message
import com.psandroidlabs.chatapp.models.MessageType
import java.util.ArrayList
import java.util.HashMap


object TicTacToeServer {
    val ticTacToeGames: HashMap<Pair<Int, Int>, ArrayList<Int>> = hashMapOf()

    fun parseMessage(message: Message) {
        val status  = when(message.type) {
            MessageType.TIC_INVITE.code -> {
                TODO()
                // return another tic_invite
            }

            MessageType.TIC_PLAY.code -> {
                checkBoard()
                // return the result of the play, valid, not valid, win or lose.
            }

            else -> {
                // return an error
            }
        }
    }

    private fun checkBoard() {
        TODO()
    }

    private fun checkWinner() {
        TODO()
    }

    private fun removeGame() {
        TODO()
    }
}