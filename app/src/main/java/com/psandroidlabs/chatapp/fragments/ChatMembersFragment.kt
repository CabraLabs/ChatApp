package com.psandroidlabs.chatapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.adapters.ChatMembersAdapter
import com.psandroidlabs.chatapp.databinding.FragmentChatMembersBinding
import com.psandroidlabs.chatapp.utils.ChatManager


class ChatMembersFragment : DialogFragment() {

    private lateinit var binding: FragmentChatMembersBinding
    private lateinit var chatMembersAdapter: ChatMembersAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.chat_members)
                .setView(binding.root)
                .setPositiveButton(
                    R.string.tictactoe_invite
                ) { _, _ ->
                }
                .setNegativeButton(
                    R.string.cancel
                ) { _, _ ->
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeList()
    }

    private fun initializeList() {
        val recyclerViewList: RecyclerView = binding.membersRecycler
        chatMembersAdapter = ChatMembersAdapter(ChatManager.chatMembersList)

        recyclerViewList.apply {
            adapter = chatMembersAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun onClick(pos: Int) {
        //TODO send tictactoe invite
    }
}