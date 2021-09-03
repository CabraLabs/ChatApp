package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentHomeBinding
import com.psandroidlabs.chatapp.utils.ChatManager
import kotlinx.coroutines.delay


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
                loading()

                ChatManager.delay(200) {
                    val action = HomeFragmentDirections.actionHomeFragmentToClientConnectFragment(null, null)
                    navController.navigate(action)
                }

            }

            serverButton.setOnClickListener {
                loading()

                ChatManager.delay(200) {
                    val action = HomeFragmentDirections.actionHomeFragmentToServerConnectFragment()
                    navController.navigate(action)
                }
            }
        }
    }

    private fun loading() {
        binding.progressBar.visibility= View.VISIBLE
    }
}