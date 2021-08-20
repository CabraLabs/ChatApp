package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.widget.ArrayAdapter
import com.psandroidlabs.chatapp.R
import kotlinx.coroutines.*
import java.net.DatagramSocket
import java.net.InetAddress

object IP : CoroutineScope {
    private val parentJob = Job()
    override val coroutineContext = parentJob + Dispatchers.IO

    /**
     * Use a datagram socket to find the device IP on the local network.
     */
    fun getIpAddress(): String {
        var ip = ""

        runBlocking {
            launch(Dispatchers.IO) {
                DatagramSocket().use { socket ->
                    socket.connect(InetAddress.getByName("8.8.8.8"), Constants.PORT_1027)
                    ip = socket.localAddress.hostAddress
                    socket.close()
                }
            }
        }

        return ip
    }

    fun getPortList(context: Context): ArrayAdapter<Int> {
        val items = listOf(Constants.PORT_1027, Constants.PORT_1028, Constants.PORT_1029)
        return ArrayAdapter(context, R.layout.list_item, items)
    }
}