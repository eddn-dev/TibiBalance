// com/app/data/alert/HabitAlertManager.kt
package com.app.data.alert

import android.app.AlarmManager
import android.content.Context
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.service.AlertManager
import kotlinx.datetime.*

class HabitAlertManager(
    private val ctx: Context,
    private val alarm: AlarmManager
) : AlertManager {

    override fun schedule(habit: Habit) {
        if (!habit.notifConfig.enabled) return

        habit.notifConfig.times.take(MAX_SLOTS).forEachIndexed { idx, time ->
            val triggerAt = nextTriggerMillis(time)
            val pi = HabitAlertReceiver.pendingIntent(
                ctx,
                requestCode(habit.id, idx),
                habit.name,
                habit.notifConfig.message
            )
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
            )
        }
    }

    override fun cancel(id: HabitId) {
        (0 until MAX_SLOTS).forEach { idx ->
            val pi = HabitAlertReceiver.pendingIntent(ctx, requestCode(id, idx), "", "")
            alarm.cancel(pi)
        }
    }

    /** Devuelve el próximo disparo para la hora [time] en **epochMillis**. */
    private fun nextTriggerMillis(time: LocalTime): Long {
        val tz          = TimeZone.currentSystemDefault()                      // :contentReference[oaicite:3]{index=3}
        val nowInstant  = Clock.System.now()                                   // :contentReference[oaicite:4]{index=4}
        val nowDateTime = nowInstant.toLocalDateTime(tz)                       // :contentReference[oaicite:5]{index=5}

        // Calculamos “hoy” a la hora indicada
        var targetDate  = nowDateTime.date
        if (nowDateTime.time >= time) {                                        // ya pasó → mañana
            targetDate = targetDate.plus(1, DateTimeUnit.DAY)                  // :contentReference[oaicite:6]{index=6}
        }

        val targetDateTime = LocalDateTime(targetDate, time)
        return targetDateTime.toInstant(tz).toEpochMilliseconds()              // :contentReference[oaicite:7]{index=7}
    }

    private fun requestCode(id: HabitId, idx: Int) = id.value.hashCode() + idx

    companion object { private const val MAX_SLOTS = 5 }
}
