package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentClientConnectBinding
import com.psandroidlabs.chatapp.models.AcceptedStatus
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel


class ClientConnectFragment : Fragment(), CoroutineScope {

    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.Main

    private val client: ClientViewModel by activityViewModels()

    private lateinit var binding: FragmentClientConnectBinding
    private val args: ClientConnectFragmentArgs by navArgs()

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onDestroy() {
        activity?.title = getString(R.string.home_app_bar_name)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.client_app_bar_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.profile -> {
                val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if(args.ip != null){
            with(binding){
                ipAddressField.setText(args.ip)
            }
        }
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

        view.setOnClickListener {
            hideKeyboard()
        }

        initializeButtons()
    }

    private fun initializeButtons() {
        //TODO args treatment
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
                            join(username)
                            val action =
                                ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(
                                    UserType.CLIENT
                                )
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

    private fun join(username: String) {
        val acceptedObserver = Observer<AcceptedStatus> {
            parseStatus(it)
        }
        client.accepted.observe(viewLifecycleOwner, acceptedObserver)

        client.writeToSocket(
            ChatManager.connectMessage(
                username,
                getString(R.string.joined_the_room),
                getPasswordField()
            )
        )
        client.readSocket()
    }

    private fun parseStatus(status: AcceptedStatus) {
        when (status) {
            AcceptedStatus.ACCEPTED -> {
                val action =
                    ClientConnectFragmentDirections.actionClientConnectFragmentToChatFragment(
                        UserType.CLIENT
                    )

                navController.navigate(action)
            }
            AcceptedStatus.WRONG_PASSWORD -> toast(getString(R.string.wrong_password))
            AcceptedStatus.SECURITY_KICK -> toast(getString(R.string.security_kick))
            AcceptedStatus.ADMIN_KICK -> toast(getString(R.string.admin_kick))
        }
    }

    private fun getPasswordField(): String {
        return binding.passwordField.text.toString().toSHA256()
    }
}