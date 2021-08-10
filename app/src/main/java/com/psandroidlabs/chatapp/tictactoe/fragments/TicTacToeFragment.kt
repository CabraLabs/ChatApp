package com.psandroidlabs.chatapp.tictactoe.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentTictactoeBinding
import com.psandroidlabs.chatapp.tictactoe.adapters.TicTacToeAdapter
import com.psandroidlabs.chatapp.tictactoe.utils.TicTacToeManager
import com.psandroidlabs.chatapp.viewmodels.TicTacToeViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class TicTacToeFragment(val hostGame: Boolean) :
    BottomSheetDialogFragment() {

    private lateinit var binding: FragmentTictactoeBinding
    private lateinit var ticTacToeAdapter: TicTacToeAdapter
    private lateinit var ticTacToeViewModel: TicTacToeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO tictactoe viewmodel
        ticTacToeViewModel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
    }

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
        binding.counter.text = TicTacToeManager.counter.toString()

        startBoard(hostGame)
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
            }
        }
        return bottomSheetDialog
    }

    private fun startBoard(hostGame: Boolean) {
        if(TicTacToeManager.counter != 1){
            ticTacToeViewModel.updateBoard(TicTacToeManager.board)
        }else {
            TicTacToeManager.fillBoard()
        }

        val recyclerViewList: RecyclerView = binding.tableRecycler
        ticTacToeAdapter = TicTacToeAdapter(TicTacToeManager.board, ::onCellClick)

        recyclerViewList.apply {
            adapter = ticTacToeAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun onCellClick(cell: String, pos: Int) {

        TicTacToeManager.markCell(pos, hostGame, ticTacToeViewModel)

        when (TicTacToeManager.identifyWinner()) {
            getString(R.string.draw) -> {
                activity?.let {
                    Snackbar.make(
                        it.findViewById(R.id.chatLayout),
                        getString(R.string.draw),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.retry)) { ticTacToeAdapter.reset() }.show()
                }
            }

            getString(R.string.player1) -> {
                activity?.let {
                    Snackbar.make(
                        it.findViewById(R.id.chatLayout),
                        "PLAYER 1 WIN!",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.retry)) { ticTacToeAdapter.reset() }.show()
                }
            }

            getString(R.string.player2) -> {
                activity?.let {
                    Snackbar.make(
                        it.findViewById(R.id.chatLayout),
                        "PLAYER 2 WIN!",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(getString(R.string.retry)) { ticTacToeAdapter.reset() }.show()
                }
            }
        }

        binding.counter.text = TicTacToeManager.counter.toString()
        binding.turn.text = TicTacToeManager.playerTurn()
    }
}
