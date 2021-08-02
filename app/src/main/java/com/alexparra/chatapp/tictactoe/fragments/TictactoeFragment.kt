package com.alexparra.chatapp.tictactoe.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.FragmentTictactoeBinding
import com.alexparra.chatapp.tictactoe.adapters.TictactoeAdapter
import com.alexparra.chatapp.tictactoe.utils.TictactoeManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class TictactoeFragment(val currentBoard: ArrayList<String>, val chatView: View?) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTictactoeBinding
    private lateinit var tictactoeAdapter: TictactoeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTictactoeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.turn.text = "Player1"
        binding.counter.text = TictactoeManager.counter.toString()

        startBoard()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState)

        bottomSheetDialog.setOnShowListener { dlgInterface ->
            val bottomSheet: ConstraintLayout? =
                (dlgInterface as BottomSheetDialog).findViewById(R.id.bottomsheet)
            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.from(it).isHideable = true
                BottomSheetBehavior.from(it).skipCollapsed = true
                BottomSheetBehavior.from(it).peekHeight = 500
            }
        }
        return bottomSheetDialog
    }

    private fun startBoard() {

        if (TictactoeManager.counter == 1)
            TictactoeManager.fillBoard()
        else
            TictactoeManager.board = currentBoard

        val recyclerViewList: RecyclerView = binding.tableRecycler
        tictactoeAdapter = TictactoeAdapter(TictactoeManager.board, ::onCellClick)

        recyclerViewList.apply {
            adapter = tictactoeAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun onCellClick(cell: String, pos: Int) {
        TictactoeManager.markCell(pos)
        TictactoeManager.identifyWinner()

        if (TictactoeManager.draw)
            dialog?.window?.let {
                Snackbar.make(it.decorView, "DRAW", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY") { tictactoeAdapter.reset() }.show()
            }

        if (TictactoeManager.player1)
            activity?.let {
                Snackbar.make(it.findViewById(R.id.chatLayout), "PLAYER 1 WIN!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY") { tictactoeAdapter.reset() }.show()
            }

        
        if (TictactoeManager.player2)
            chatView?.let {
                Snackbar.make(it, "PLAYER 2 WIN!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY") { tictactoeAdapter.reset() }.show()
            }

        binding.counter.text = TictactoeManager.counter.toString()
        binding.turn.text = TictactoeManager.playerTurn()
    }
}
