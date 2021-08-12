package com.psandroidlabs.chatapp.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentServerConnectBinding
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.IP
import com.psandroidlabs.chatapp.utils.hideKeyboard
import com.psandroidlabs.chatapp.utils.toast
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import com.psandroidlabs.chatapp.viewmodels.ServerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress


class ServerConnectFragment : Fragment(), CoroutineScope {

    private val client: ClientViewModel by activityViewModels()
    private val server: ServerViewModel by activityViewModels()

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var binding: FragmentServerConnectBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onDestroy() {
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

        view.setOnClickListener {
            hideKeyboard()
        }

        showIp()

        val serverObserver = Observer<Boolean> {
            changeButtons(it)
        }
        server.serverRunning.observe(viewLifecycleOwner, serverObserver)

        initializeButtons()
    }

    private fun initializeButtons() {
        with(binding) {
            userNameField.setText(R.string.admin)

            createAndJoin.setOnClickListener {
                if (usernameText()) {
                    loading(true)

                    server.startServerService(activity as Activity)
                    join()
                }
            }

            createServer.setOnClickListener {
                server.startServerService(activity as Activity)
                server.updateServerState(true)
                changeButtons(true)
            }

            joinChat.setOnClickListener {
                if (usernameText()) {
                    loading(true)

                    join()
                }
            }

            stopServer.setOnClickListener {
                context?.let { server.stopServer(it) }
                server.updateServerState(false)
                changeButtons(true)
            }
        }
    }

    private fun join() {
        with(binding) {
            ChatManager.delay(1000) {
                val success = client.startSocket(userNameField.text.toString(), InetAddress.getByName(ipAddress.text.toString()))

                if (success) {
                    loading(false)

                    server.updateServerState(true)

                    val action = ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(UserType.SERVER)
                    navController.navigate(action)
                } else {
                    loading(false)
                    toast(getString(R.string.port_in_use_error))
                }
            }

            changeButtons(true)
        }
    }

    private fun changeButtons(change: Boolean) {
        with(binding) {
            if (change) {
                createAndJoin.visibility = View.GONE
                createServer.visibility = View.GONE

                joinChat.visibility = View.VISIBLE
                stopServer.visibility = View.VISIBLE
            } else {
                createAndJoin.visibility = View.VISIBLE
                createServer.visibility = View.VISIBLE

                joinChat.visibility = View.GONE
                stopServer.visibility = View.GONE
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

    private fun usernameText(): Boolean {
        with(binding) {
            return if (userNameField.text.toString() == "") {
                toast(getString(R.string.username_missing))
                username.error = getString(R.string.username_missing)
                false
            } else {
                true
            }
        }
    }

    private fun showIp() {
        binding.ipAddress.text = IP.getIpAddress()
    }
}