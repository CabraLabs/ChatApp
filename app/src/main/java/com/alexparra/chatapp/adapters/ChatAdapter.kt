package com.alexparra.chatapp.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R

class ChatAdapter(private val dataSet: ArrayList<String>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewUsername: TextView
        val textViewMessage: TextView
        val textViewDate: TextView
        val chatLayout: LinearLayout

        init {
            textViewUsername = view.findViewById(R.id.chat_row_username)
            textViewMessage = view.findViewById(R.id.chat_row_message)
            textViewDate = view.findViewById(R.id.chat_row_data)
            chatLayout = view.findViewById(R.id.chatLayout)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.chat_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val row = dataSet[position].split(";")
        viewHolder.textViewUsername.text = row[1]
        viewHolder.textViewMessage.text = row[2]
        viewHolder.textViewDate.text = row[3]

        if(row[0] == "e"){
            viewHolder.textViewUsername.gravity = Gravity.CENTER
            viewHolder.textViewMessage.gravity = Gravity.CENTER
            viewHolder.textViewDate.gravity = Gravity.CENTER
        }
        if(row[0] == "c"){
            viewHolder.textViewUsername.gravity = Gravity.LEFT
            viewHolder.textViewMessage.gravity = Gravity.LEFT
            viewHolder.textViewDate.gravity = Gravity.LEFT
        }
        if(row[0] == "s"){
            viewHolder.textViewUsername.gravity = Gravity.RIGHT
            viewHolder.textViewMessage.gravity = Gravity.RIGHT
            viewHolder.textViewDate.gravity = Gravity.RIGHT
        }

    }

    override fun getItemCount() = dataSet.size
}