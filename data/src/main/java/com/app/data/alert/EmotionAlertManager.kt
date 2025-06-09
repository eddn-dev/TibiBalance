// :data/alert/EmotionAlertManager.kt
package com.app.data.alert

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

/**
 * Maneja la programación del recordatorio diario para registrar emociones.
 */
class EmotionAlertManager(
    private val ctx: Context,
    private val alarm: AlarmManager
) {
    /** Programa la próxima alarma exacta a la [time] indicada. */
    fun schedule(time: LocalTime) {
        val tz = TimeZone.currentSystemDefault()
        var target = Clock.System.todayIn(tz).atTime(time).toInstant(tz)
        if (target < Clock.System.now()) target += 1.days            // la hora de hoy ya pasó

        val pi = pendingIntent(ctx)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarm.canScheduleExactAlarms()

        if (canExact)
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.toEpochMilliseconds(),
                pi
            )
        else
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                target.toEpochMilliseconds(),
                pi
            )
    }

    /** Cancela cualquier recordatorio pendiente. */
    fun cancel() = alarm.cancel(pendingIntent(ctx))

    /* ---------- helpers ---------- */

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, EmotionReminderReceiver::class.java)
        val flags  = PendingIntent.FLAG_UPDATE_CURRENT or
                // MUTABLE para que el sistema pueda inyectar RemoteInput
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    PendingIntent.FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(context, REQ_CODE, intent, flags)
    }

    private companion object { const val REQ_CODE = 999 }
}
