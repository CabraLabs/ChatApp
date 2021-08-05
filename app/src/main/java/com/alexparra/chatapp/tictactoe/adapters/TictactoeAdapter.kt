package com.alexparra.chatapp.tictactoe.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.TableCellBinding
import com.alexparra.chatapp.tictactoe.utils.TictactoeManager

class TictactoeAdapter (
    private var dataSet: ArrayList<String>,
    private val onClick: (String, Int) -> Unit
) :
    RecyclerView.Adapter<TictactoeAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(string: String, position: Int)
    }

    inner class CellViewHolder(private val binding: TableCellBinding): ViewHolder(binding.root){
        override fun bind(string: String, position: Int) {

            with(binding){
                val imgRes = when (dataSet[position]) {
                    "x" -> R.drawable.ic_x
                    "o" -> R.drawable.ic_o
                    else -> R.drawable.empty
                }

                cellImageId.setImageResource(imgRes)

                btnCell.setOnClickListener {
                    onClick.invoke(dataSet[position], position)
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return CellViewHolder(TableCellBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position], position)
    }

    override fun getItemCount() = dataSet.size

    fun reset() {
        TictactoeManager.reset()
        dataSet = TictactoeManager.board
        notifyDataSetChanged()
    }
}