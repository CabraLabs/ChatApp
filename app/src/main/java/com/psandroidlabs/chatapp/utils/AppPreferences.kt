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

            putString(Constants.CLIENT_USER, clientUsername)
            putString(Constants.CLIENT_IP, clientIp)

            apply()
        }
    }

    fun getClient(context: Context?): ArrayList<String> {
        val sharedClient = getSharedClient(context)

        return arrayListOf(
            (sharedClient?.getString(Constants.CLIENT_USER, "")).toString(),
            (sharedClient?.getString(Constants.CLIENT_IP, "")).toString()
        )
    }
}