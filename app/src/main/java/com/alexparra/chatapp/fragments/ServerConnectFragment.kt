package com.alexparra.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.databinding.FragmentServerConnectBinding
import com.alexparra.chatapp.models.Server
import kotlinx.coroutines.*
import java.net.InetAddress

class ServerConnectFragment : Fragment(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var binding: FragmentServerConnectBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServerConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeButtons()
    }

    private fun initializeButtons() {

        with(binding) {
            launch(Dispatchers.IO) {
                val ip = InetAddress.getLocalHost()

                withContext(Dispatchers.Main) {
                    ipAddress.text = ip.toString()
                }
            }

            createServer.setOnClickListener {
                launch(Dispatchers.IO) {
                    val server = Server()

                    withContext(Dispatchers.Main) {
                        val action = ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(server)

                        navController.navigate(action)
                    }
                }
            }
        }
    }
}