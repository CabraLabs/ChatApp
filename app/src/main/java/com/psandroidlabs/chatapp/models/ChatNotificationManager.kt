package com.psandroidlabs.chatapp.models

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.psandroidlabs.chatapp.ActionManager
import com.psandroidlabs.chatapp.MainActivity
import com.psandroidlabs.chatapp.MainApplication.Companion.applicationContext
import com.psandroidlabs.chatapp.R
import com.psandroidlabs.chatapp.utils.ChatManager
import com.psandroidlabs.chatapp.utils.Constants
import com.psandroidlabs.chatapp.utils.IP


class ChatNotificationManager(val context: Context, private val channel: String) {

    private val notificationManager by lazy {
        ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel= NotificationChannel(channel, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun sendMessage(username: String, message: String) {
        createChannel()

        val intent = Intent(applicationContext(), MainActivity::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(
            context,
            channel
        ).apply {
            setSmallIcon(R.drawable.ic_text_message)
            setContentTitle(username)
            setContentText(message)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
            color = context.getColor(R.color.blue)
        }

        showNotification(notification = builder)
    }

    fun foregroundNotification(): Notification {
        createChannel()

        val ip = applicationContext().getString(R.string.server_on_ip, IP.getIpAddress())

        val closeIntent = Intent(context, ActionManager::class.java).apply {
            action = Constants.ACTION_STOP
        }
        val closePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 100, closeIntent, 0)

        val closeAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stop,
            context.getString(R.string.close_server),
            closePendingIntent
        ).build()

        return NotificationCompat.Builder(context, channel)
            .setColor(context.getColor(R.color.blue))
            .setContentTitle(context.getString(R.string.server_running))
            .setContentText(ip)
            .setSmallIcon(R.drawable.ic_server_foreground)
            .addAction(closeAction)
            .build()
    }

    private fun showNotification(channel: Int = 0, notification: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            notify(channel, notification.build())
        }
        ChatManager.playSound()
    }

    fun cancelNotification() {
        notificationManager.cancelAll()
    }
}