package com.alexparra.chatapp.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        with(viewHolder) {

            textViewUsername.text = dataSet[position].username
            textViewMessage.text = dataSet[position].message
            textViewDate.text = dataSet[position].time

            when (dataSet[position].type) {
                MessageType.SENT -> {
                    rowLayout.gravity = Gravity.START
                    messageBackground.gravity = Gravity.START
                    messageBackground.orientation = LinearLayout.VERTICAL
                    messageBackground.setBackgroundResource(R.drawable.send_chat_bg)
                    angryFace.visibility = View.GONE
                    textViewMessage.visibility = View.VISIBLE
                    textViewDate.visibility = View.VISIBLE
                    textViewUsername.setTypeface(null, Typeface.NORMAL)
                    textViewMessage.setTypeface(null, Typeface.NORMAL)
                }

                MessageType.RECEIVED -> {
                    rowLayout.gravity = Gravity.END
                    messageBackground.gravity = Gravity.END
                    messageBackground.orientation = LinearLayout.VERTICAL
                    messageBackground.setBackgroundResource(R.drawable.receive_chat_bg)
                    angryFace.visibility = View.GONE
                    textViewMessage.visibility = View.VISIBLE
                    textViewDate.visibility = View.VISIBLE
                    textViewUsername.setTypeface(null, Typeface.NORMAL)
                    textViewMessage.setTypeface(null, Typeface.NORMAL)
                }

                MessageType.JOINED -> {
                    rowLayout.gravity = Gravity.CENTER_HORIZONTAL
                    messageBackground.gravity = Gravity.CENTER_HORIZONTAL
                    messageBackground.orientation = LinearLayout.HORIZONTAL
                    messageBackground.setBackgroundResource(R.drawable.event_chat_bg)
                    angryFace.visibility = View.GONE
                    textViewMessage.visibility = View.VISIBLE
                    textViewDate.visibility = View.VISIBLE
                    textViewUsername.setTypeface(null, Typeface.ITALIC)
                    textViewMessage.setTypeface(null, Typeface.ITALIC)
                }

                MessageType.ATTENTION -> {
                    rowLayout.gravity = Gravity.CENTER_HORIZONTAL
                    messageBackground.gravity = Gravity.CENTER_HORIZONTAL
                    messageBackground.orientation = LinearLayout.HORIZONTAL
                    messageBackground.setBackgroundResource(R.drawable.event_chat_bg)
                    angryFace.visibility = View.VISIBLE
                    textViewMessage.visibility = View.GONE
                    textViewDate.visibility = View.GONE
                    textViewUsername.setTypeface(null, Typeface.NORMAL)
                    textViewMessage.setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }

    override fun getItemCount() = dataSet.size
}