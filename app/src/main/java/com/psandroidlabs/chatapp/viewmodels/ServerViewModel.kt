package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.psandroidlabs.chatapp.ServerService
import com.psandroidlabs.chatapp.utils.Constants

class ServerViewModel : ViewModel() {
    private var serverRunning = false

    fun updateServerState(isRunning: Boolean) {
        serverRunning = isRunning
    }

    fun getServerState() = serverRunning

    fun startServerService(activity: Activity) {
        val intent = Intent(activity, ServerService::class.java)
        ContextCompat.startForegroundService(activity.applicationContext, intent)
    }

    fun stopServer(context: Context) {
        val broadcastIntent = Intent(Constants.ACTION_STOP)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }
}