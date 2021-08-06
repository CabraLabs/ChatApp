package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.psandroidlabs.chatapp.R

object AppPreferences {

    private fun getSharedClient(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences(context.getString(R.string.client_user), Context.MODE_PRIVATE)
    }

    fun saveClient(clientUsername: String, clientIp: String, context: Context?){
        val sharedClient = getSharedClient(context) ?: return
        with(sharedClient.edit()) {
            clear()
            putString("client_user", clientUsername)
            putString("client_ip", clientIp)
            apply()
        }
    }

    fun getClient(context: Context?): ArrayList<String> {
        val sharedClient = getSharedClient(context)
        return arrayListOf(
            (sharedClient?.getString("client_user", "")).toString(),
            (sharedClient?.getString("client_ip", "")).toString()
        )
    }

    fun disconnectClient(context: Context) {
        getSharedClient(context)!!.edit().clear().apply()
    }
}