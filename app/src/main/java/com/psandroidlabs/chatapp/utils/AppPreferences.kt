package com.psandroidlabs.chatapp.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    private fun getSharedClient(context: Context?): SharedPreferences? {
        return context?.getSharedPreferences(Constants.CLIENT_USER, Context.MODE_PRIVATE)
    }

    fun saveClient(clientUsername: String, clientIp: String, clientAvatar: String?,  context: Context?) {
        val sharedClient = getSharedClient(context) ?: return

        with(sharedClient.edit()) {
            clear()

            putString(Constants.CLIENT_USER, clientUsername)
            putString(Constants.CLIENT_IP, clientIp)
            putString(Constants.CLIENT_AVATAR, clientAvatar)

            apply()
        }
    }

    fun getClient(context: Context?): ArrayList<String> {
        val sharedClient = getSharedClient(context)

        return arrayListOf(
            (sharedClient?.getString(Constants.CLIENT_USER, "")).toString(),
            (sharedClient?.getString(Constants.CLIENT_IP, "")).toString(),
            (sharedClient?.getString(Constants.CLIENT_AVATAR, "")).toString()
        )
    }
}