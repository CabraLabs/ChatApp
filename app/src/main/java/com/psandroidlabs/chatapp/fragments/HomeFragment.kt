package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_fragment_menu, menu)
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
                val action = HomeFragmentDirections.actionHomeFragmentToClientConnectFragment(null)
                navController.navigate(action)
            }

            serverButton.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToServerConnectFragment()
                navController.navigate(action)
            }
        }
    }
}