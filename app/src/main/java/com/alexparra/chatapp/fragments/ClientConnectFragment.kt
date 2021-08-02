package com.alexparra.chatapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.alexparra.chatapp.R
import com.alexparra.chatapp.databinding.FragmentClientConnectBinding
import com.alexparra.chatapp.models.ClientSocket
import com.alexparra.chatapp.utils.AppPreferences
import com.alexparra.chatapp.utils.toast
import kotlinx.coroutines.*
import java.net.Inet4Address

class ClientConnectFragment : Fragment(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

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
                        ipAddressField.error = getString(R.string.ip_missing)
                    }

                    else -> {
                        launch(Dispatchers.IO) {
                            try {
                                val inetAddress = Inet4Address.getByName(ipAddressField.text.toString())
                                val client = ClientSocket(userNameField.text.toString(), inetAddress, 1027)

                                AppPreferences.saveClient(
                                    userNameField.text.toString(),
                                    ipAddressField.text.toString(),
                                    context
                                )

                                withContext(Dispatchers.Main) {
                                    val action =
                                        ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(
                                            client
                                        )

                                    navController.navigate(action)
                                }
                            } catch (e: java.net.ConnectException) {
                                withContext(Dispatchers.Main) {
                                    toast(getString(R.string.connect_error))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}