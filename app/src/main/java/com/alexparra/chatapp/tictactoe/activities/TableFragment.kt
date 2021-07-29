package com.alexparra.chatapp.tictactoe.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.databinding.FragmentTableBinding
import com.alexparra.chatapp.tictactoe.adapters.TableAdapter
import com.alexparra.chatapp.tictactoe.utils.TableManager
import com.google.android.material.snackbar.Snackbar

class TableFragment  : Fragment() {

    private lateinit var binding: FragmentTableBinding
    private lateinit var homeAdapter: TableAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun onCellClick(cell: String, pos: Int) {
            TableManager.markCell(pos)
        TableManager.identifyWinner()
        if(TableManager.draw)
            Snackbar.make(view as View, "DRAW", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        if(TableManager.player1)
            Snackbar.make(view as View, "PLAYER 1 WIN!", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        if(TableManager.player2)
            Snackbar.make(view as View, "PLAYER 2 WIN!", Snackbar.LENGTH_INDEFINITE).setAction("RETRY"){homeAdapter.reset()}.show()
        binding.counter.text = TableManager.counter.toString()
        binding.turn.text = TableManager.playerTurn()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TableManager.fillBoard()
        binding.turn.text = "Player1"
        binding.counter.text = TableManager.counter.toString()

        val recyclerViewList: RecyclerView = binding.tableRecycler
        homeAdapter = TableAdapter(TableManager.board, ::onCellClick)


        recyclerViewList.apply {
            adapter = homeAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }
}