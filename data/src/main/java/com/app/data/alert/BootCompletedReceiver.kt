package com.app.data.alert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
// Import the concrete class
import com.app.data.alert.HabitNotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitScheduler: HabitNotificationScheduler // Injecting the concrete class

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Using a simple CoroutineScope for this background task.
            // For longer operations, consider HiltWorker or goAsync() with a dedicated scope.
            CoroutineScope(Dispatchers.IO).launch {
                habitScheduler.rescheduleAllEnabledHabitNotifications()
            }
        }
    }
}
