package com.app.data.alert

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
// import com.app.data.R // TODO: Use this if R.drawable.ic_notification is available in data module
import com.app.domain.enums.NotifMode // For NotifMode.SOUND.name
import com.app.tibibalance.MainActivity // For launching MainActivity

class HabitAlertReceiver : BroadcastReceiver() {

    companion object {
        // This channel ID should match the one created in TibiBalanceApp
        // and defined in NotifChannel.HABITS.id
        const val CHANNEL_HABITS = "habits"

        // New Extra keys from HabitNotificationScheduler
        const val EXTRA_HABIT_ID = "habitId"
        const val EXTRA_HABIT_NAME = "habitName"
        const val EXTRA_NOTIFICATION_MESSAGE = "notificationMessage"
        const val EXTRA_NOTIFICATION_TIME_NANO = "notificationTimeNano"
        const val EXTRA_NOTIFICATION_MODE_STR = "notificationModeStr"
        const val EXTRA_VIBRATE_STR = "vibrateStr"

        // Key for MainActivity intent extra, to navigate or highlight the specific habit
        const val HABIT_ID_INTENT_EXTRA_KEY = "HABIT_ID_EXTRA"

        private const val TAG = "HabitAlertReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received alert: ${intent.action}")

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME)
        val notificationMessage = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE)
        // GetLongExtra requires a default value. 0L is fine for nano if not present, though it's essential.
        val notificationTimeNano = intent.getLongExtra(EXTRA_NOTIFICATION_TIME_NANO, 0L)
        val notificationModeStr = intent.getStringExtra(EXTRA_NOTIFICATION_MODE_STR)
        val vibrateStr = intent.getStringExtra(EXTRA_VIBRATE_STR)

        if (habitId == null || habitName == null || notificationMessage == null) {
            Log.e(TAG, "Essential notification data missing in intent. Cannot show notification.")
            Log.e(TAG, "HabitID: $habitId, Name: $habitName, Message: $notificationMessage, TimeNano: $notificationTimeNano")
            return
        }

        Log.d(TAG, "Processing notification for Habit ID: $habitId, Name: $habitName, TimeNano: $notificationTimeNano")

        // Create PendingIntent for MainActivity to open when notification is tapped
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            // Standard flags to bring existing task to front or start new one
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pass habitId to MainActivity so it can potentially navigate to the habit's detail
            putExtra(HABIT_ID_INTENT_EXTRA_KEY, habitId)
        }

        // Request code for PendingIntent should be unique enough for this purpose, habitId's hashcode is fine.
        val pendingActivityIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingActivityIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),  // Using habitId's hashcode as request code
            mainActivityIntent,
            pendingActivityIntentFlags
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_HABITS)
            // TODO: Replace with com.app.data.R.drawable.ic_notification or a common module icon when available
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(habitName)
            .setContentText(notificationMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for reminders
            .setAutoCancel(true) // Notification disappears when tapped
            .setContentIntent(pendingActivityIntent) // Intent to launch when tapped

        var defaults = 0
        if (notificationModeStr == NotifMode.SOUND.name) {
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
            Log.d(TAG, "Notification for $habitId will have sound.")
        }
        if (vibrateStr == "true") {
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
            Log.d(TAG, "Notification for $habitId will have vibration.")
        }

        // Only set defaults if sound or vibration is explicitly requested.
        // Otherwise, it will use the defaults specified on the NotificationChannel.
        if (defaults != 0) {
            notificationBuilder.setDefaults(defaults)
        } else {
            Log.d(TAG, "Notification for $habitId will use channel defaults for sound/vibration.")
        }

        // Unique ID for the notification itself: habitId + specific time to allow multiple notifs for same habit
        val uniqueNotificationId = "${habitId}_${notificationTimeNano}".hashCode()

        val notificationManager = NotificationManagerCompat.from(context)

        try {
            notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
            Log.i(TAG, "Notification shown for habit '$habitName' (ID: $uniqueNotificationId)")
        } catch (e: SecurityException) {
            // This might happen if POST_NOTIFICATIONS permission is revoked runtime
            Log.e(TAG, "SecurityException while trying to show notification. Check POST_NOTIFICATIONS permission.", e)
        }
    }
}