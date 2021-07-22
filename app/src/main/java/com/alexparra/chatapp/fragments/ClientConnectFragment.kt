package com.alexparra.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.FragmentClientConnectBinding
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.utils.toast

class ClientConnectFragment : Fragment() {

    private lateinit var binding: FragmentClientConnectBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeButtons()
    }

    private fun initializeButtons() {
        with(binding) {
            joinChat.setOnClickListener {
                when {
                    username.text.toString() == ""  -> {
                        toast(getString(R.string.username_missing))
                    }

                    ipAddress.text.toString() == "" -> {
                        toast(getString(R.string.ip_missing))
                    }

                    else -> {
                        val client = ClientSocket(username.text.toString(), ipAddress.text.toString(), 13)

                        val action = ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(client)

                        navController.navigate(action)
                    }
                }
            }
        }
    }
}