package com.alexparra.chatapp.tictactoe.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.tictactoe.utils.TableManager

class TableAdapter (
    private var dataSet: ArrayList<String>,
    private val onClick: (String, Int) -> Unit
) :
    RecyclerView.Adapter<TableAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cell_image: ImageView
        val btn_cell: LinearLayout

        init {
            cell_image = view.findViewById(R.id.cell_image_id)
            btn_cell = view.findViewById(R.id.btn_cell)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.table_cell, viewGroup, false)

        return HomeViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val imgRes = when (dataSet[position]) {
            "x" -> R.drawable.ic_x
            "o" -> R.drawable.ic_o
            else -> R.drawable.empty
        }

        holder.cell_image.setImageResource(imgRes)

        holder.btn_cell.setOnClickListener {
            onClick.invoke(dataSet[position], position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = dataSet.size

    @SuppressLint("NotifyDataSetChanged")
    fun reset() {
        TableManager.fillBoard()
        dataSet = TableManager.board
        TableManager.counter = 1
        TableManager.player1Turn = true
        TableManager.draw = false
        TableManager.player1 = false
        TableManager.player2 = false
        notifyDataSetChanged()
    }
}