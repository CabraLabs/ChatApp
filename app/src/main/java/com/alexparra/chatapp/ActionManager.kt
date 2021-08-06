package com.alexparra.chatapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alexparra.chatapp.utils.Constants

class ActionManager : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }

        val broadcastIntent = Intent(Constants.ACTION_STOP)
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
    }
}