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
 * Recibe la alarma y muestra la notificación del hábito.
 * Al tocarla se abre la aplicación (launcher activity).
 */
class HabitAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra(EXTRA_TITLE)   ?: context.getString(R.string.app_name)
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: context.getString(R.string.app_name)
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)   // por si en el futuro se navega

        /* ── PendingIntent que abre la Main/LANZADORA ── */
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_HABIT_ID, habitId)          // opcional
            }

        val contentPiFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_IMMUTABLE else 0)

        val contentPi = PendingIntent.getActivity(
            context,
            habitId?.hashCode() ?: 0,                      // requestCode
            launchIntent,
            contentPiFlags
        )

        /* ── Notificación ── */
        val notification = NotificationCompat.Builder(context, CHANNEL_HABITS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(title.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_HABITS = "habits"

        private const val EXTRA_TITLE    = "title"
        private const val EXTRA_MESSAGE  = "msg"
        private const val EXTRA_HABIT_ID = "hid"

        /** PendingIntent que programa el AlarmManager */
        fun pendingIntent(
            ctx: Context,
            requestCode: Int,
            habitId: String,
            title: String,
            message: String
        ): PendingIntent {
            val intent = Intent(ctx, HabitAlertReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_HABIT_ID, habitId)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0)
            return PendingIntent.getBroadcast(ctx, requestCode, intent, flags)
        }
    }
}
