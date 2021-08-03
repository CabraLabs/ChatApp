package com.alexparra.chatapp.viewmodel

import androidx.lifecycle.ViewModel
import com.alexparra.chatapp.models.ClientSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class ServerViewModel(private val port: Int = 1027) : ViewModel() {
    private val server: ServerSocket by lazy {
        ServerSocket(this.port)
    }

    fun getSocket(): ServerSocket {
        return server
    }

    fun acceptLoop() {

    }

    fun userConnected() {
        
    }
}

class ClientViewModel(
    private val ip: InetAddress,
    val username: String
    ) : ViewModel() {
    private val client: Socket by lazy {
        Socket(ip, 1027)
    }

    fun getSocket(): Socket {
        return client
    }
}