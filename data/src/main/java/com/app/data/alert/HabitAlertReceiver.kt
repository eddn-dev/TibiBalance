package com.app.data.alert

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.data.R

/**
 * Recibe las alarmas programadas y muestra la notificación del hábito.
 */
class HabitAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE)
            ?: context.getString(R.string.app_name)
        val message = intent.getStringExtra(EXTRA_MESSAGE)
            ?: context.getString(R.string.app_name)

        val notification = NotificationCompat.Builder(context, CHANNEL_HABITS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(title.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_HABITS = "habits"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_MESSAGE = "msg"

        fun pendingIntent(
            ctx: Context,
            requestCode: Int,
            title: String,
            message: String
        ): PendingIntent {
            val intent = Intent(ctx, HabitAlertReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            return PendingIntent.getBroadcast(ctx, requestCode, intent, flags)
        }
    }
}