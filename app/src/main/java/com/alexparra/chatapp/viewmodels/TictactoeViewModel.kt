package com.alexparra.chatapp.viewmodels

import androidx.lifecycle.ViewModel

class TictactoeViewModel: ViewModel() {
    private var board = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9")

    fun getCurrentBoard(): ArrayList<String>{
        return board
    }

    fun updateBoard(newBoard: ArrayList<String>){
        board = newBoard
    }
}