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
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


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
        return when (item.itemId) {
            R.id.profile -> {
                val action = ClientConnectFragmentDirections.actionClientConnectFragmentToProfileFragment()
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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

        val acceptedObserver = Observer<AcceptedStatus?> {
            if (it != null) {
                parseStatus(it)
            }
        }
        client.accepted.observe(viewLifecycleOwner, acceptedObserver)

        initializeButtons()
        portNumbers()
    }

    private fun initializeButtons() {
        //TODO args treatment deeplink
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
                toast(getString(R.string.connect_error))
            }
        }
    }

    private fun join(username: String) {
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