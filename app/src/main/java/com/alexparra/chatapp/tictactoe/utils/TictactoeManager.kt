package com.alexparra.chatapp.tictactoe.utils

import android.content.res.Resources
import android.widget.Toast
import com.alexparra.chatapp.MainApplication
import com.alexparra.chatapp.R
import com.alexparra.chatapp.models.Chat
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.models.Server
import com.alexparra.chatapp.viewmodels.TictactoeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.util.*
import kotlin.collections.ArrayList
import com.alexparra.chatapp.models.ClientSocket


object TictactoeManager {
    var board: ArrayList<String> = ArrayList()
    var counter = 1
    var player1Turn = true

    fun fillBoard() {
        board = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
    }

    fun playerTurn(): String {
        if (player1Turn)
            return Resources.getSystem().getString(R.string.player1)
        return Resources.getSystem().getString(R.string.player2)
    }

    fun boardToString(newBoard: ArrayList<String>): String {
        return newBoard.joinToString(
            prefix = "BOARD:",
            separator = "/",
            transform = { it }
        )
    }

    fun stringToBoard(string: String): ArrayList<String> {
        string.replace("BOARD:", "")
        return ArrayList((string.split("/")))
    }

    fun sendMessage(chat: Chat, message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                chat.writeToSocket(message)
            } catch (e: java.net.SocketException) {
                Toast.makeText(
                    MainApplication.applicationContext(),
                    Resources.getSystem().getString(R.string.snack_server_disconnect),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun receiveMessageListener(string: String) {
        board = stringToBoard(string)
    }

    fun markCell(position: Int, chat: Chat, tictactoeViewModel: TictactoeViewModel) {
        if (!player1Win() && !player2Win()) {
            if (player1Turn && chat is Server) {
                board[position] = "x"
                tictactoeViewModel.updateBoard(board)
                sendMessage(chat, boardToString(board))
                counter++
                player1Turn = !player1Turn
            } else if (!player1Turn && chat is ClientSocket) {
                board[position] = "o"
                tictactoeViewModel.updateBoard(board)
                sendMessage(chat, boardToString(board))
                counter++
                player1Turn = !player1Turn
            }
        }
    }

    fun identifyWinner(): String {
        if (counter == 10 && !player1Win() && !player2Win()) {
            return Resources.getSystem().getString(R.string.draw)
        }
        if (player1Win()) {
            return Resources.getSystem().getString(R.string.player1)
        }
        if (player2Win()) {
            return Resources.getSystem().getString(R.string.player2)
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