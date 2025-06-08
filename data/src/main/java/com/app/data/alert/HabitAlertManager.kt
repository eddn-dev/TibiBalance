// :data/alert/HabitAlertManager.kt
package com.app.data.alert

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.service.AlertManager
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.minutes

class HabitAlertManager(
    private val ctx  : Context,
    private val alarm: AlarmManager
) : AlertManager {

    private fun ensureExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = ctx.getSystemService(AlarmManager::class.java)
        if (am.canScheduleExactAlarms()) return true

        /* abre Ajustes – FLAG_ACTIVITY_NEW_TASK evita la API deprecated */
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
        return false
    }

    override fun schedule(habit: Habit) {
        if (!ensureExactAlarmPermission()) return   // ← no crashea
        val cfg = habit.notifConfig
        if (!cfg.enabled) return                                // 🚫 notificaciones desactivadas

        val tz    = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(tz)

        // rango de fechas permitido
        val afterStart  = cfg.startsAt?.let { today >= it } ?: true
        val beforeEnd   = cfg.expiresAt?.let { today <= it }   ?: true
        if (!(afterStart && beforeEnd)) return                 // fuera de rango

        // genera lista de horas (vacío ⇒ [null])
        val baseTimes = if (cfg.times.isEmpty()) listOf<LocalTime?>(null) else cfg.times

        baseTimes.take(MAX_BASE_SLOTS).forEachIndexed { baseIdx, baseTime ->
            val trigger0 = computeFirstTrigger(baseTime, cfg.advanceMin, tz)

            // alarma principal
            scheduleOne(
                habit      = habit,
                epochMs    = trigger0,
                baseIdx    = baseIdx,
                repeatIdx  = 0
            )

            // alarmas de repetición (snooze)
            repeat(cfg.repeatQty.coerceAtMost(MAX_REPEATS)) { rpt ->
                val epoch = trigger0 + (cfg.snoozeMin * (rpt + 1)).minutes.inWholeMilliseconds
                scheduleOne(
                    habit      = habit,
                    epochMs    = epoch,
                    baseIdx    = baseIdx,
                    repeatIdx  = rpt + 1
                )
            }
        }
    }

    override fun cancel(id: HabitId) {
        (0 until MAX_BASE_SLOTS).forEach { base ->
            (0 .. MAX_REPEATS).forEach { rpt ->
                val pi = HabitAlertReceiver.pendingIntent(
                    ctx,
                    requestCode(id, base, rpt),
                    "", "",
                    message = ""
                )
                alarm.cancel(pi)
            }
        }
    }

    /* ───────── helpers ───────── */

    private fun scheduleOne(
        habit    : Habit,
        epochMs  : Long,
        baseIdx  : Int,
        repeatIdx: Int
    ) {
        val pi = HabitAlertReceiver.pendingIntent(
            ctx,
            requestCode(habit.id, baseIdx, repeatIdx),
            habit.id.raw,               // ← nuevo arg
            habit.name,
            habit.notifConfig.message
        )

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarm.canScheduleExactAlarms()

        if (canExact) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMs, pi)
        } else {
            alarm.setAndAllowWhileIdle   (AlarmManager.RTC_WAKEUP, epochMs, pi)
        }
    }


    private fun computeFirstTrigger(
        scheduledTime: LocalTime?,          // null ⇒ “cualquier hora”
        advanceMin   : Int,
        tz           : TimeZone
    ): Long {
        val now   = Clock.System.now().toLocalDateTime(tz)
        var date  = now.date
        val time  = scheduledTime ?: LocalTime(0, 0)

        if (now.time >= time) date = date.plus(1, DateTimeUnit.DAY)      // ya pasó → mañana

        val triggerInstant = LocalDateTime(date, time)
            .toInstant(tz)                   // LocalDateTime → Instant
            .minus(advanceMin.minutes)       // resta Duration

        return triggerInstant.toEpochMilliseconds()
    }

    private fun requestCode(id: HabitId, baseIdx: Int, repeatIdx: Int): Int =
        id.raw.hashCode() * 31 + baseIdx * 10 + repeatIdx

    companion object {
        private const val MAX_BASE_SLOTS = 10        // máx. horas distintas por hábito
        private const val MAX_REPEATS    = 5         // máx. repeticiones por hora
    }


}
