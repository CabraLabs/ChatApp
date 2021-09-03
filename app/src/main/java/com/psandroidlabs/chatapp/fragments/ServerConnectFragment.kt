package com.psandroidlabs.chatapp.fragments

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.databinding.FragmentServerConnectBinding
import com.psandroidlabs.chatapp.models.AcceptedStatus
import com.psandroidlabs.chatapp.models.UserType
import com.psandroidlabs.chatapp.utils.*
import com.psandroidlabs.chatapp.viewmodels.ClientViewModel
import com.psandroidlabs.chatapp.viewmodels.ServerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.net.InetAddress


@DelicateCoroutinesApi
class ServerConnectFragment : Fragment() {

    private val client: ClientViewModel by activityViewModels()
    private val server: ServerViewModel by activityViewModels()

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
        setHasOptionsMenu(true)
        activity?.title = getString(R.string.server_app_bar_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                val action =
                    ServerConnectFragmentDirections.actionServerConnectFragmentToProfileFragment()
                navController.navigate(action)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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

        server.isServerServiceRunning()
        initializeButtons()
        portNumbers()
    }

    private fun initializeButtons() {
        loadPreferences()

        removeErrorListener()

        with(binding) {
            userNameField.setText(R.string.admin)

            radioGroup.setOnCheckedChangeListener { group, _ ->
                when (group.checkedRadioButtonId) {
                    R.id.showPassword -> showPasswordField(true)
                    R.id.hidePassword -> showPasswordField(false)
                }
            }

            createAndJoin.setOnClickListener {
                if (checkFields()) {
                    loading(true)

                    if (showPassword.isChecked) {
                        server.startServerService(
                            activity as Activity,
                            getPasswordField(),
                            getPortField()
                        )
                    } else {
                        server.startServerService(activity as Activity, port = getPortField())
                    }

                    join()
                }
            }

            createServer.setOnClickListener {
                enableRadioButtons(false)
                changeButtons(true)
                passwordFieldEnable(false)

                password.isEnabled = false

                if (showPassword.isChecked) {
                    if (checkFields()) {
                        server.startServerService(
                            activity as Activity,
                            getPasswordField(),
                            getPortField()
                        )
                    }
                } else {
                    server.startServerService(activity as Activity, port = getPortField())
                }
            }

            joinChat.setOnClickListener {
                if (checkFields()) {
                    loading(true)

                    join()
                }
            }

            stopServer.setOnClickListener {
                context?.let { server.stopServer(it) }
                server.updateServerState(false)
                changeButtons(false)
            }
        }
    }

    private fun loadPreferences() {
        val preference = AppPreferences.getClient(context)

        with(binding) {
            if (preference.isNotEmpty()) {

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

    private fun join() {
        with(binding) {
            ChatManager.delay(200) {
                val success = client.startSocket(
                    userNameField.text.toString(),
                    InetAddress.getByName(ipAddress.text.toString()),
                    getPortField()
                )

                if (success) {
                    loading(false)
                    server.updateServerState(true)

                    connect(userNameField.text.toString())

                } else {
                    loading(false)
                    toast(getString(R.string.port_in_use_error))
                }
            }

            changeButtons(true)
        }
    }

    private fun connect(username: String) {
        client.accepted.observe(viewLifecycleOwner) {
            if (it != null) {
                parseStatus(it)
            }
        }

        client.readSocket()

        if (binding.showPassword.isChecked) {
            client.writeToSocket(
                ChatManager.connectMessage(
                    username,
                    getString(R.string.created_the_room),
                    getPasswordField()
                )
            )
        } else {
            client.writeToSocket(
                ChatManager.connectMessage(
                    username,
                    getString(R.string.created_the_room)
                )
            )
        }
    }

    private fun parseStatus(status: AcceptedStatus) {
        when (status) {
            AcceptedStatus.ACCEPTED -> {
                val action =
                    ServerConnectFragmentDirections.actionServerConnectFragmentToChatFragment(
                        UserType.SERVER
                    )

                navController.navigate(action)
            }
            AcceptedStatus.WRONG_PASSWORD -> toast(getString(R.string.wrong_password))
            AcceptedStatus.SECURITY_KICK -> toast(getString(R.string.security_kick))
            AcceptedStatus.ADMIN_KICK -> toast(getString(R.string.admin_kick))
            AcceptedStatus.MISSING_ID -> toast(getString(R.string.missing_id))
        }
    }

    private fun showPasswordField(isActive: Boolean) {
        with(binding) {
            if (isActive) {
                password.apply {
                    visibility = View.VISIBLE
                }
            } else {
                password.apply {
                    visibility = View.GONE
                }
            }
        }
    }

    private fun changeButtons(change: Boolean) {
        with(binding) {
            if (change) {
                enableRadioButtons(false)
                passwordFieldEnable(false)

                port.isEnabled = false
                username.isEnabled = false

                createAndJoin.visibility = View.GONE
                createServer.visibility = View.GONE

                joinChat.visibility = View.VISIBLE
                stopServer.visibility = View.VISIBLE
            } else {
                enableRadioButtons(true)
                passwordFieldEnable(true)

                port.isEnabled = true
                username.isEnabled = true

                createAndJoin.visibility = View.VISIBLE
                createServer.visibility = View.VISIBLE

                joinChat.visibility = View.GONE
                stopServer.visibility = View.GONE

                portNumbers()
            }
        }
    }

    private fun enableRadioButtons(isEnabled: Boolean) {
        with(binding) {
            showPassword.isEnabled = isEnabled
            hidePassword.isEnabled = isEnabled
        }
    }

    private fun loading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                enableRadioButtons(false)
                createServer.apply {
                    alpha = 0.3F
                    isClickable = false
                }
            } else {
                progressBar.visibility = View.GONE
                enableRadioButtons(true)
                createServer.apply {
                    alpha = 1F
                    isClickable = true
                }
            }
        }
    }

    private fun checkFields(): Boolean {
        with(binding) {
            return if (userNameField.text.toString() == "") {
                usernameFieldError()
                false
            } else if (showPassword.isChecked) {
                if (passwordField.text.toString() == "") {
                    passwordFieldError()
                    false
                } else {
                    true
                }
            } else {
                true
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

            passwordField.setOnClickListener {
                if (password.error != null) {
                    password.error = null
                }
            }
        }
    }

    private fun passwordFieldEnable(isActive: Boolean) {
        binding.password.isEnabled = isActive
    }

    private fun usernameFieldError() {
        binding.username.error = getString(R.string.username_missing)
    }

    private fun passwordFieldError() {
        binding.password.error = getString(R.string.password_missing)
    }

    private fun showIp() {
        binding.ipAddress.text = IP.getIpAddress()
    }

    private fun getPasswordField(): String {
        return binding.passwordField.text.toString().toSHA256()
    }

    private fun getPortField(): Int {
        return binding.portField.text.toString().toInt()
    }
}