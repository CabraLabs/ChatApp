package com.alexparra.chatapp.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.databinding.FragmentTableBinding
import com.alexparra.chatapp.tictactoe.adapters.TictactoeAdapter
import com.alexparra.chatapp.tictactoe.utils.TictactoeManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class TictactoeFragment  : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTableBinding
    private lateinit var homeAdapter: TictactoeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onCellClick(cell: String, pos: Int) {
            TictactoeManager.markCell(pos)
        TictactoeManager.identifyWinner()
        if(TictactoeManager.draw)
            Snackbar.make(view as View, "DRAW", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        if(TictactoeManager.player1)
            Snackbar.make(view as View, "PLAYER 1 WIN!", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        if(TictactoeManager.player2)
            Snackbar.make(view as View, "PLAYER 2 WIN!", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        binding.counter.text = TictactoeManager.counter.toString()
        binding.turn.text = TictactoeManager.playerTurn()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TictactoeManager.fillBoard()
        binding.turn.text = "Player1"
        binding.counter.text = TictactoeManager.counter.toString()

        startTable()
    }

    fun startTable(){
        val recyclerViewList: RecyclerView = binding.tableRecycler
        homeAdapter = TictactoeAdapter(TictactoeManager.board, ::onCellClick)

        recyclerViewList.apply {
            adapter = homeAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }
}