package com.psandroidlabs.chatapp.utils

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
}