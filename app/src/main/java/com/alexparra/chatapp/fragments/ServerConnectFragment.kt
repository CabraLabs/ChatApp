package com.alexparra.chatapp.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.R
import com.alexparra.chatapp.ServerService
import com.alexparra.chatapp.databinding.FragmentServerConnectBinding
import com.alexparra.chatapp.models.UserType
import com.alexparra.chatapp.utils.toast

import com.alexparra.chatapp.viewmodel.ClientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress

class ServerConnectFragment : Fragment(), CoroutineScope {

    private val client: ClientViewModel by activityViewModels()

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private lateinit var binding: FragmentServerConnectBinding

    private val navController: NavController by lazy {
        findNavController()
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ip = client.getIpAddress()

        showIp(ip)
        initializeButtons(ip)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeButtons(ip: String) {

        with(binding) {
            userNameField.setText(R.string.admin)

            createServer.setOnClickListener {
                if (userNameField.text.toString() == "") {
                    toast(getString(R.string.username_missing))
                    username.error = getString(R.string.username_missing)
                } else {
                    loading(true)

                    toast(getString(R.string.waiting_for_a_connection))


                    val success = client.startSocket(userNameField.text.toString(), InetAddress.getByName(ip))

                    if (success) {
                        loading(false)

                        // TODO CONTINUE
                        val intent = Intent(activity, ServerService::class.java)
                        ContextCompat.startForegroundService(requireContext(), intent)

                        val action = ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(UserType.SERVER)
                        navController.navigate(action)
                    } else {
                        loading(false)
                        toast(getString(R.string.port_in_use_error))
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showIp(ip: String) {
        binding.ipAddress.text = ip
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
}