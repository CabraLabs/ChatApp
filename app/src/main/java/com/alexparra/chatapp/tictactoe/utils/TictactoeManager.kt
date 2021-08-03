package com.alexparra.chatapp.tictactoe.utils

import com.alexparra.chatapp.models.Chat
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.models.Server

object TictactoeManager {
    var board: ArrayList<String> = ArrayList()
    var counter = 1
    var player1Turn = true

    fun fillBoard() {
        board = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
    }

    fun playerTurn(): String {
        if (player1Turn)
            return "Player 1"
        return "Player 2"
    }

    fun markCell(position: Int, chat: Chat) {
        if (!player1Win() && !player2Win()) {
            if (player1Turn && chat is Server) {
                board[position] = "x"
                counter++
                player1Turn = !player1Turn
            } else if(!player1Turn && chat is ClientSocket){
                board[position] = "o"
                counter++
                player1Turn = !player1Turn
            }
        }
    }

    fun identifyWinner(): String {
        if (counter == 10 && !player1Win() && !player2Win()) {
            return "draw"
        }
        if (player1Win()) {
            return "player1"
        }
        if (player2Win()) {
            return "player2"
        }
        return "none"
    }

    fun player1Win(): Boolean {
        if ((board[0] == "x" && board[1] == "x" && board[2] == "x") ||
            (board[3] == "x" && board[4] == "x" && board[5] == "x") ||
            (board[6] == "x" && board[7] == "x" && board[8] == "x") ||
            (board[0] == "x" && board[4] == "x" && board[8] == "x") ||
            (board[2] == "x" && board[4] == "x" && board[6] == "x") ||
            (board[0] == "x" && board[3] == "x" && board[6] == "x") ||
            (board[1] == "x" && board[4] == "x" && board[7] == "x") ||
            (board[2] == "x" && board[5] == "x" && board[8] == "x")
        ) return true
        return false
    }

    fun player2Win(): Boolean {
        if ((board[0] == "o" && board[1] == "o" && board[2] == "o") ||
            (board[3] == "o" && board[4] == "o" && board[5] == "o") ||
            (board[6] == "o" && board[7] == "o" && board[8] == "o") ||
            (board[0] == "o" && board[4] == "o" && board[8] == "o") ||
            (board[2] == "o" && board[4] == "o" && board[6] == "o") ||
            (board[0] == "o" && board[3] == "o" && board[6] == "o") ||
            (board[1] == "o" && board[4] == "o" && board[7] == "o") ||
            (board[2] == "o" && board[5] == "o" && board[8] == "o")
        ) return true
        return false
    }

    fun reset() {
        fillBoard()
        counter = 1
        player1Turn = true
    }
}