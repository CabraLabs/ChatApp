package com.alexparra.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.FragmentServerConnectBinding
import com.alexparra.chatapp.models.Server
import com.alexparra.chatapp.utils.toast
import com.alexparra.chatapp.viewmodel.ServerViewModel
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class ServerConnectFragment : Fragment(), CoroutineScope {


    private val model: ServerViewModel by activityViewModels()

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var binding: FragmentServerConnectBinding
    private lateinit var server: Server

    private var SERVER_INITIALIZED = false

    private val navController: NavController by lazy {
        findNavController()
    }


    override fun onDestroy() {
        if (SERVER_INITIALIZED) {
            closeServer()
        }

        activity?.title = getString(R.string.home_app_bar_name)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.server_app_bar_name)
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
                loading(true)

                launch(Dispatchers.IO) {
                    try {
                        server = Server()

                        withContext(Dispatchers.Main) {
                            toast(getString(R.string.waiting_for_a_connection))
                        }

                        SERVER_INITIALIZED = true

                        try {
                            server.startServer()
                        } catch (e: SocketException) {
                            return@launch
                        }

                        withContext(Dispatchers.Main) {
                            loading(false)
                            val action = ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(server)
                            navController.navigate(action)
                        }
                    } catch (e: java.net.BindException) {
                        withContext(Dispatchers.Main) {
                            loading(false)
                            toast(getString(R.string.port_in_use_error))
                            closeServer()
                        }
                    }
                }
            }
        }
    }

    private fun showIp() {
        launch(Dispatchers.IO) {
            DatagramSocket().use { socket ->
                socket.connect(InetAddress.getByName("8.8.8.8"), 1027)
                val ip = socket.localAddress.hostAddress
                binding.ipAddress.text = ip.toString()
                socket.close()
            }
        }
    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                createServer.apply {
                    alpha = 0.3F
                    isClickable = false
                }
            } else {
                progressBar.visibility = View.GONE
                createServer.apply {
                    alpha = 1F
                    isClickable = true
                }
            }
        }
    }

    private fun closeServer() {
        runBlocking {
            launch(Dispatchers.IO) {
                try {
                    server.closeSocket()
                } catch (e: SocketException) {
                    return@launch
                }
            }
        }
    }
}