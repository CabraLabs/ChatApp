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
import java.net.Inet4Address

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

        showIp()
        initializeButtons()
    }

    private fun initializeButtons() {

        with(binding) {
            createServer.setOnClickListener {
                launch(Dispatchers.IO) {

                    val server = Server()

                    withContext(Dispatchers.Main) {
                        val action = ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(server)

                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun showIp() {
        launch(Dispatchers.IO) {
            val ip = Inet4Address.getLocalHost()
            binding.ipAddress.text = ip.toString()
        }
    }
}