package com.app.dr1009.addbu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

enum class NotificationChannelList(val channelName: Int, val channelDescription: Int, val channelId: String) {
    TILE_SERVICE(R.string.notification_tile_title, R.string.notification_tile_description, "tile_channel");

    companion object {
        fun registerNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            NotificationChannelList.values().forEach {
                // Create the NotificationChannel
                val name = context.getString(it.channelName)
                val descriptionText = context.getString(it.channelDescription)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(it.channelId, name, importance)
                channel.description = descriptionText
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService<NotificationManager>()
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }
}