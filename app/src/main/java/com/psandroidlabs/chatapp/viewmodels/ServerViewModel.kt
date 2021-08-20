package com.psandroidlabs.chatapp.viewmodels

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.ServerService
import com.psandroidlabs.chatapp.utils.Constants

class ServerViewModel : ViewModel() {
    val serverRunning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constants.ACTION_STOP) {
                updateServerState(false)
            }
        }
    }

    fun updateServerState(isRunning: Boolean) {
        serverRunning.value = isRunning
    }

    fun startServerService(activity: Activity, password: String? = null, port: Int) {
        val intent = Intent(activity, ServerService::class.java).apply {
            putExtra(Constants.PASSWORD, password)
            putExtra(Constants.PORT, port)
        }
        ContextCompat.startForegroundService(activity.applicationContext, intent)

        LocalBroadcastManager.getInstance(applicationContext()).registerReceiver(receiver, IntentFilter(Constants.ACTION_STOP))
    }

    fun stopServer(context: Context) {
        val broadcastIntent = Intent(Constants.ACTION_STOP)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }
}