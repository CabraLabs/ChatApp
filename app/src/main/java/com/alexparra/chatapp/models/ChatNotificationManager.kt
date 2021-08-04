package com.alexparra.chatapp.models

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.alexparra.chatapp.MainActivity
import com.alexparra.chatapp.R
import com.alexparra.chatapp.ServerService

@RequiresApi(Build.VERSION_CODES.O)
class ChatNotificationManager(val context: Context, val channel: String) {


    private val notificationChannel: NotificationChannel = NotificationChannel(channel, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)

    private val notificationManager: NotificationManager =
        ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager


    fun sendMessage(username: String, text: String, activity: Activity) {
        notificationManager.createNotificationChannel(notificationChannel)

        val intent = Intent(activity, MainActivity::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(
            context,
            channel
        ).apply {
            setSmallIcon(R.drawable.ic_text_message)
            setContentTitle(username)
            setContentText(text)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
            color = context.getColor(R.color.blue)
        }

        showNotification(notification = builder)
    }

    fun foregroundNotification(serverInfo: String): Notification {
        val intent = Intent(context, ServerService::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        return Notification.Builder(context, "foreground")
            .setColor(context.getColor(R.color.blue))
            .setContentTitle(context.getString(R.string.server_running))
            .setContentText(serverInfo)
            .setSmallIcon(R.drawable.ic_server_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showNotification(channel: Int = 0, notification: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            notify(channel, notification.build())
        }
    }

    fun cancelNotification() {
        notificationManager.cancelAll()
    }
}