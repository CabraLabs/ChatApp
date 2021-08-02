package com.alexparra.chatapp.tictactoe.utils

object TictactoeManager {
    var board: ArrayList<String> = ArrayList()
    var counter = 1
    var player1Turn = true
    var draw = false
    var player1 = false
    var player2 = false

    fun fillBoard() {
        board = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
    }

    fun playerTurn(): String {
        if (player1Turn)
            return "Player 1"
        return "Player 2"
    }

    fun markCell(position: Int) {
        if (!player1Win() && !player2Win()) {
            if (player1Turn) {
                board[position] = "x"
            } else {
                board[position] = "o"
            }
            counter++
            player1Turn = !player1Turn
        }
    }

    fun identifyWinner() {
        if (counter == 10 && !player1Win() && !player2Win()) {
            draw = true
        }
        if (player1Win()) {
            player1 = true
        }
        if (player2Win()) {
            player2 = true
        }
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
        draw = false
        player1 = false
        player2 = false
    }
}