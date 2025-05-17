package com.app.data.alert

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.service.AlertManager

/**
 * Implementación de [AlertManager] basada en [AlarmManager].
 */
class HabitAlertManager(
    private val ctx: Context,
    private val alarm: AlarmManager
) : AlertManager {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun schedule(habit: Habit) {
        if (!habit.notifConfig.enabled) return
        habit.notifConfig.times.forEachIndexed { index, time ->
            val triggerAt = nextTriggerMillis(time)
            val pi = HabitAlertReceiver.pendingIntent(
                ctx,
                requestCode(habit.id, index),
                habit.name,
                habit.notifConfig.message
            )
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    override fun cancel(id: HabitId) {
        // Cancelamos todas las notificaciones usando el mismo esquema de requestCode
        (0 until MAX_SLOTS).forEach { index ->
            val pi = HabitAlertReceiver.pendingIntent(
                ctx,
                requestCode(id, index),
                "",
                ""
            )
            alarm.cancel(pi)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nextTriggerMillis(time: LocalTime): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var target = LocalDate.now(zone).atTime(time)
        if (target.isBefore(now)) {
            target = target.plusDays(1)
        }
        return target.atZone(zone).toInstant().toEpochMilli()
    }

    private fun requestCode(id: HabitId, index: Int): Int = id.value.hashCode() + index

    companion object {
        private const val MAX_SLOTS = 5
    }
}