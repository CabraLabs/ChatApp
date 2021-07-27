package com.alexparra.chatapp.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alexparra.chatapp.R

@RequiresApi(Build.VERSION_CODES.O)
class ChatNotificationManager(val context: Context, val channel: String) {

    private val notificationChannel: NotificationChannel = NotificationChannel(channel, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)

    private val notificationManager: NotificationManager =
        ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager

    fun sendMessage(username: String, text: String) {
        notificationManager.createNotificationChannel(notificationChannel)

        val builder = NotificationCompat.Builder(
            context,
            channel
        ).apply {
            setSmallIcon(R.drawable.ic_text_message)
            setContentTitle(username)
            setContentText(text)
            priority = NotificationCompat.PRIORITY_HIGH
            color = context.getColor(R.color.blue)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }
}