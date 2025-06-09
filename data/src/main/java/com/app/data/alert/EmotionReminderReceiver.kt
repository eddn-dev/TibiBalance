// :data/alert/EmotionReminderReceiver.kt
package com.app.data.alert

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.data.R
import kotlinx.datetime.Clock

class EmotionReminderReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIF_ID = 987654
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(ctx: Context, intent: Intent) {

        val notif = NotificationCompat.Builder(ctx, HabitAlertReceiver.CHANNEL_HABITS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(ctx.getString(R.string.emotion_reminder_title))
            .setContentText(ctx.getString(R.string.emotion_reminder_msg))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(NOTIF_ID, notif)
    }
}
