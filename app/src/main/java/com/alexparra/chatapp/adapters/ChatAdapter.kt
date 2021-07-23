package com.alexparra.chatapp.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexparra.chatapp.R
import com.alexparra.chatapp.models.Message
import com.alexparra.chatapp.models.MessageType

class ChatAdapter(private val dataSet: ArrayList<Message>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewUsername: TextView
        val textViewMessage: TextView
        val textViewDate: TextView
        val rowLayout: LinearLayout

        init {
            textViewUsername = view.findViewById(R.id.chat_row_username)
            textViewMessage = view.findViewById(R.id.chat_row_message)
            textViewDate = view.findViewById(R.id.chat_row_data)
            rowLayout = view.findViewById(R.id.rowLayout)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.receive_chat_row_item, viewGroup, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        if(dataSet[position].type == MessageType.SENT)
            viewHolder.rowLayout.gravity = Gravity.RIGHT

        if(dataSet[position].type == MessageType.RECEIVED)
            viewHolder.rowLayout.gravity = Gravity.LEFT

        viewHolder.textViewUsername.text = dataSet[position].username
        viewHolder.textViewMessage.text = dataSet[position].message
        viewHolder.textViewDate.text = dataSet[position].time
    }

    override fun getItemCount() = dataSet.size
}