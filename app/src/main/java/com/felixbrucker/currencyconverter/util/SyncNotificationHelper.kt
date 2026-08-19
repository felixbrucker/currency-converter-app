package com.felixbrucker.currencyconverter.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.felixbrucker.currencyconverter.MainActivity

object SyncNotificationHelper {
    private const val CHANNEL_ID = "sync_errors_channel"
    private const val NOTIFICATION_ID = 1001

    fun showSyncErrorNotification(context: Context, errorMessage: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sync Errors",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for exchange rate synchronization errors"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Replace with app icon if available
            .setContentTitle("Sync Failed")
            .setContentText(errorMessage ?: "An error occurred while updating exchange rates.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
