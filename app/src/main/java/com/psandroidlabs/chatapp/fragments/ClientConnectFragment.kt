package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentClientConnectBinding
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.AppPreferences
import com.psandroidlabs.chatapp.utils.toast
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.Inet4Address


class ClientConnectFragment : Fragment(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private val client: ClientViewModel by activityViewModels()

    private lateinit var binding: FragmentClientConnectBinding

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onDestroy() {
        activity?.title = getString(R.string.home_app_bar_name)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.client_app_bar_name)
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
            if (AppPreferences.getClient(context)[0].isNotBlank()) {
                userNameField.setText(AppPreferences.getClient(context)[0])
                ipAddressField.setText(AppPreferences.getClient(context)[1])
            }

            joinChat.setOnClickListener {
                when {
                    userNameField.text.toString() == "" -> {
                        toast(getString(R.string.username_missing))
                        username.error = getString(R.string.username_missing)
                    }

                    ipAddressField.text.toString() == "" -> {
                        toast(getString(R.string.ip_missing))
                        ipAddress.error = getString(R.string.ip_missing)
                    }

                    else -> {
                        val username = userNameField.text.toString()
                        val inetAddress = client.transformIp(ipAddressField.text.toString())
                        val success = client.startSocket(username, inetAddress)

                        if (success) {
                            AppPreferences.saveClient(
                                username,
                                ipAddressField.text.toString(),
                                context
                            )

                            val action =
                                ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(UserType.CLIENT)
                            navController.navigate(action)
                        } else {
                            toast(getString(R.string.connect_error))
                            ipAddress.error
                        }
                    }
                }
            }
        }
    }
}