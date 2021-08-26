package com.psandroidlabs.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.databinding.ChatMembersRowBinding
import com.psandroidlabs.chatapp.models.Profile


class ChatMembersAdapter(
    private var dataSet: ArrayList<Profile>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ChatMembersAdapter.ViewHolder>() {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(profile: Profile, position: Int)
    }

    inner class CellViewHolder(private val binding: ChatMembersRowBinding) : ViewHolder(binding.root) {
        override fun bind(profile: Profile, position: Int) {
            with(binding) {
                chatMembersUsername.text = dataSet[position].name

                btnChatMember.setOnClickListener {
                    onClick.invoke(position)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return CellViewHolder(
            ChatMembersRowBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position], position)
    }

    override fun getItemCount() = dataSet.size
}