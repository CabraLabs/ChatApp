package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.psandroidlabs.chatapp.adapters.ChatMembersAdapter
import com.psandroidlabs.chatapp.databinding.FragmentChatMembersBinding
import com.psandroidlabs.chatapp.utils.ChatManager

class ChatMembersFragment: Fragment() {

    private lateinit var binding: FragmentChatMembersBinding
    private lateinit var chatMembersAdapter: ChatMembersAdapter

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

    private fun initializeList(){
        val recyclerViewList: RecyclerView = binding.tableRecycler
        chatMembersAdapter = ChatMembersAdapter(ChatManager.chatMembersList, ::onCellClick)

        recyclerViewList.apply {
            adapter = chatMembersAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun onCellClick(pos: Int) {
        //TODO send tictactoe invite
    }
}