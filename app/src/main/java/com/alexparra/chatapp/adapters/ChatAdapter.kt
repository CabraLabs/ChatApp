package com.alexparra.chatapp.adapters

import android.graphics.Color
import android.opengl.Visibility
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val angryFace: ImageView
        val messageBackground: LinearLayout

        init {
            textViewUsername = view.findViewById(R.id.chat_row_username)
            textViewMessage = view.findViewById(R.id.chat_row_message)
            textViewDate = view.findViewById(R.id.chat_row_data)
            rowLayout = view.findViewById(R.id.rowLayout)
            angryFace = view.findViewById(R.id.angry_face)
            messageBackground = view.findViewById(R.id.messageBackground)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.chat_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        if(dataSet[position].type == MessageType.SENT) {
            viewHolder.rowLayout.gravity = Gravity.START
            viewHolder.messageBackground.setBackgroundColor(Color.parseColor("#FFDBDB"))
            viewHolder.textViewUsername.setTextColor(Color.parseColor("#E91A1A"))
        }

        if(dataSet[position].type == MessageType.RECEIVED) {
            viewHolder.rowLayout.gravity = Gravity.END
            viewHolder.messageBackground.setBackgroundColor(Color.parseColor("#DBE0FF"))
            viewHolder.textViewUsername.setTextColor(Color.parseColor("#1A58E9"))
        }

        if(dataSet[position].type == MessageType.JOINED) {
            viewHolder.rowLayout.gravity = Gravity.CENTER_HORIZONTAL
            viewHolder.messageBackground.setBackgroundColor(Color.parseColor("#F1EFEF"))
            viewHolder.textViewUsername.setTextColor(Color.parseColor("#1B1B1B"))
        }

        if(dataSet[position].type == MessageType.ATTENTION) {
            viewHolder.rowLayout.gravity = Gravity.CENTER_HORIZONTAL
            viewHolder.messageBackground.setBackgroundColor(Color.parseColor("#F1EFEF"))
            viewHolder.textViewUsername.setTextColor(Color.parseColor("#1B1B1B"))
            viewHolder.angryFace.visibility = View.VISIBLE
            viewHolder.textViewMessage.visibility = View.GONE
        }

        viewHolder.textViewUsername.text = dataSet[position].username
        viewHolder.textViewMessage.text = dataSet[position].message
        viewHolder.textViewDate.text = dataSet[position].time
    }

    override fun getItemCount() = dataSet.size
}