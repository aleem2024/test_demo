package com.aleem.jamaahreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Jamaah Reminder"
        val body = intent.getStringExtra("body") ?: ""
        val extra = intent.getStringExtra("extra") ?: ""
        val code = intent.getIntExtra("code", 1)

        val fullIntent = Intent(context, ReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", body)
            putExtra("extra", extra)
        }
        val fullPi = PendingIntent.getActivity(
            context, code, fullIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "jamaah_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Jamaah Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen Jamaah and Jumu'ah reminders"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body.replace("\n", " "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(listOf(body, extra).filter { it.isNotBlank() }.joinToString("\n\n")))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(fullPi)
            .setFullScreenIntent(fullPi, true)
            .build()

        nm.notify(code, notification)

        try {
            context.startActivity(fullIntent)
        } catch (_: Exception) {}
    }
}
