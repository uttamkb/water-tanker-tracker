package com.apartment.watertracker.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID_ALERTS = "tanker_alerts"
    private const val CHANNEL_NAME_ALERTS = "Tanker Alerts"
    
    private const val CHANNEL_ID_DUPLICATE = "duplicate_entries"
    private const val CHANNEL_NAME_DUPLICATE = "Duplicate entry alerts"

    private const val CHANNEL_ID_INVENTORY = "inventory_alerts"
    private const val CHANNEL_NAME_INVENTORY = "Water Inventory Alerts"

    fun showDuplicateWarning(context: Context, title: String, message: String) {
        showNotification(context, CHANNEL_ID_DUPLICATE, CHANNEL_NAME_DUPLICATE, title, message, 1001)
    }

    fun showTankerArrivalNotification(context: Context, title: String, message: String) {
        showNotification(context, CHANNEL_ID_ALERTS, CHANNEL_NAME_ALERTS, title, message, 1002)
    }

    fun showBidNotification(context: Context, title: String, message: String) {
        showNotification(context, CHANNEL_ID_ALERTS, CHANNEL_NAME_ALERTS, title, message, 1003)
    }

    fun showLowWaterAlert(context: Context, title: String, message: String) {
        showNotification(context, CHANNEL_ID_INVENTORY, CHANNEL_NAME_INVENTORY, title, message, 1004)
    }

    private fun showNotification(
        context: Context, 
        channelId: String, 
        channelName: String, 
        title: String, 
        message: String,
        notificationId: Int
    ) {
        if (!ensureChannel(context, channelId, channelName)) return
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Replace with app icon later
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun ensureChannel(context: Context, channelId: String, channelName: String): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(channelId)
        if (existing != null) return true

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
        return true
    }
}
