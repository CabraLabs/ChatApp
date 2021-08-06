package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeButtons()
    }

    private fun initializeButtons() {
        with(binding) {
            clientButton.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToClientConnectFragment()
                navController.navigate(action)
            }

            serverButton.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToServerConnectFragment()
                navController.navigate(action)
            }
        }
    }
}