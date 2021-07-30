package com.alexparra.chatapp.models

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.alexparra.chatapp.MainActivity
import com.alexparra.chatapp.R

@RequiresApi(Build.VERSION_CODES.O)
class ChatNotificationManager(val context: Context, val channel: String) {


    private val notificationChannel: NotificationChannel = NotificationChannel(channel, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)

    private val notificationManager: NotificationManager =
        ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager


    fun sendMessage(username: String, text: String, activity: Activity) {
        notificationManager.createNotificationChannel(notificationChannel)

        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(activity)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(intent)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

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

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }

    fun cancelNotification() {
        notificationManager.cancelAll()
    }
}