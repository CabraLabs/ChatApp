package com.psandroidlabs.chatapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentClientConnectBinding
import com.psandroidlabs.chatapp.models.AcceptedStatus
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.*
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class ClientConnectFragment : Fragment() {

    private val client: ClientViewModel by activityViewModels()

    private lateinit var binding: FragmentClientConnectBinding
    private val args: ClientConnectFragmentArgs by navArgs()

    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onDestroy() {
        client.closeSocket()
        activity?.title = getString(R.string.home_app_bar_name)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (args.ip != null) {
            with(binding) {
                ipAddressField.setText(args.ip)
                portField.setText(args.port)
            }
        }
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
        return when (item.itemId) {
            R.id.profile -> {
                val action = ClientConnectFragmentDirections.actionClientConnectFragmentToProfileFragment()
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
        binding = FragmentClientConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view.setOnClickListener {
            hideKeyboard()
        }

        initializeButtons()
        portNumbers()
    }

    private fun initializeButtons() {
        with(binding) {
            loadPreferences()

            removeErrorListener()

            joinChat.setOnClickListener {
                when {
                    userNameField.text.toString() == "" -> {
                        username.error = getString(R.string.username_missing)
                    }

                    ipAddressField.text.toString() == "" -> {
                        ipAddress.error = getString(R.string.ip_missing)
                    }

                    else -> {
                        removeErrorListener()
                        validate()
                    }
                }
            }
        }
    }

    private fun loadPreferences() {
        val preference = AppPreferences.getClient(context)

        with(binding) {
            if (preference.isNotEmpty()) {
                userNameField.setText(AppPreferences.getClient(context)[0])
                ipAddressField.setText(AppPreferences.getClient(context)[1])

                if (preference[3].isNullOrBlank()) {
                    portField.setText(preference[3], false)
                } else {
                    portField.setText(Constants.PORT_1027.toString(), false)
                }
            }
        }
    }

    private fun portNumbers() {
        val adapter = IP.getPortList(requireContext())
        binding.portField.setAdapter(adapter)
    }

    private fun validate() {
        if (!IP.validateIp(getIpAddressField())) {
            binding.ipAddress.error = getString(R.string.invalid_ip)
            return
        } else {
            loading(true)

            val username = binding.userNameField.text.toString()
            val inetAddress = client.transformIp(getIpAddressField())
            val success = client.startSocket(username, inetAddress, getPortField())

            if (success) {
                AppPreferences.saveClient(
                    username,
                    getIpAddressField(),
                    getPortField().toString(),
                    null,
                    context
                )

                join(username)
            } else {
                loading(false)
                toast(getString(R.string.connect_error))
            }
        }
    }

    private fun join(username: String) {
        client.accepted.observe(viewLifecycleOwner) {
            if (it != null) {
                parseStatus(it)
            }
        }

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

            AcceptedStatus.WRONG_PASSWORD -> {
                loading(false)
                toast(getString(R.string.wrong_password))
            }

            AcceptedStatus.SECURITY_KICK -> {
                loading(false)
                toast(getString(R.string.security_kick))
            }

            AcceptedStatus.ADMIN_KICK -> {
                loading(false)
                toast(getString(R.string.admin_kick))
            }

            AcceptedStatus.MISSING_ID -> {
                loading(false)
                toast(getString(R.string.missing_id))
            }
        }
    }

    private fun removeErrorListener() {
        with(binding) {
            userNameField.setOnClickListener {
                if (username.error != null) {
                    username.error = null
                }
            }

            ipAddressField.setOnClickListener {
                if (ipAddress.error != null) {
                    ipAddress.error = null
                }
            }
        }
    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                joinChat.isEnabled = false

            } else {
                progressBar.visibility = View.GONE
                joinChat.isEnabled = true
            }
        }
    }

    private fun getPasswordField(): String {
        return binding.passwordField.text.toString().toSHA256()
    }

    private fun getIpAddressField(): String {
        return binding.ipAddressField.text.toString()
    }

    private fun getPortField(): Int {
        return binding.portField.text.toString().toInt()
    }
}