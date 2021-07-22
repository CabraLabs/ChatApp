package com.alexparra.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R

class ChatAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewUsername: TextView
        val textViewMessage: TextView
        val textViewDate: TextView

        init {
            textViewUsername = view.findViewById(R.id.chat_row_username)
            textViewMessage = view.findViewById(R.id.chat_row_message)
            textViewDate = view.findViewById(R.id.chat_row_data)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.chat_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val row = dataSet[position].split(";;")
        viewHolder.textViewUsername.text = row[0]
        viewHolder.textViewMessage.text = row[1]
        viewHolder.textViewDate.text = row[2]
    }

    override fun getItemCount() = dataSet.size
}