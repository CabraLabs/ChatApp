package com.alexparra.chatapp.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.FragmentServerConnectBinding
import com.alexparra.chatapp.models.UserType
import com.alexparra.chatapp.utils.toast
import com.alexparra.chatapp.viewmodel.ServerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ServerConnectFragment : Fragment(), CoroutineScope {


    private val server: ServerViewModel by activityViewModels()

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var binding: FragmentServerConnectBinding

    private var SERVER_INITIALIZED = false

    private val navController: NavController by lazy {
        findNavController()
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showIp()
        initializeButtons()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeButtons() {

        with(binding) {
            createServer.setOnClickListener {
                loading(true)

                toast(getString(R.string.waiting_for_a_connection))

                val success = server.startServer()

                SERVER_INITIALIZED = true

                if (success) {
                    loading(false)
                    val action = ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(UserType.SERVER)
                    navController.navigate(action)
                } else {
                    loading(false)
                    toast(getString(R.string.port_in_use_error))
                    closeServer()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showIp() {
        binding.ipAddress.text = server.getIpAddress()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun closeServer() {
        server.closeServer()
    }
}