package com.app.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.common.SyncMeta
import com.app.domain.config.*
import com.app.domain.entities.*
import com.app.domain.enums.*
import com.app.domain.ids.HabitId
import com.app.domain.model.HabitForm
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.DayOfWeek
import java.util.*

private fun Instant.atStartOfDaySystem(): Instant =
    toLocalDateTime(TimeZone.currentSystemDefault())
        .date
        .atStartOfDayIn(TimeZone.currentSystemDefault())

@RequiresApi(Build.VERSION_CODES.O)
fun HabitForm.toHabit(
    id: HabitId = HabitId(UUID.randomUUID().toString()),
    now: Instant = Clock.System.now()
): Habit {

    /* sesión */
    val session = Session(sessionQty, sessionUnit)

    /* repetición + periodo */
    val repeat  = repeatPreset.toRepeat(weekDays)
    val period  = Period(periodQty, periodUnit)

    /* notificaciones */
    val notif = if (!notify) {
        NotifConfig(enabled = false)
    } else {
        NotifConfig(
            enabled    = true,
            message    = notifMessage.ifBlank { "¡Es hora!" },
            times      = notifTimes.map(LocalTime::parse),
            pattern    = repeat,
            advanceMin = notifAdvanceMin
        )
    }

    /* reto (ChallengeConfig) */
    val challengeCfg = if (challenge) {
        val start = now.atStartOfDaySystem()

        val end = if (periodQty != null && periodUnit != PeriodUnit.INDEFINIDO) {
            when (periodUnit) {
                PeriodUnit.DIAS    -> start + DateTimePeriod(days   = periodQty!!)
                PeriodUnit.SEMANAS -> start + DateTimePeriod(days   = periodQty!! * 7)
                PeriodUnit.MESES   -> start + DateTimePeriod(months = periodQty!!)
                PeriodUnit.INDEFINIDO -> start + DateTimePeriod(days = 21)
            }
        } else {
            start + DateTimePeriod(days = 21)           // fallback 21 días
        }

        ChallengeConfig(
            start         = start,
            end           = end,
            currentStreak = 0,
            totalSessions = 0
        )
    } else null

    return Habit(
        id          = id,
        name        = name,
        description = desc,
        category    = category,
        icon        = icon,
        session     = session,
        repeat      = repeat,
        period      = period,
        notifConfig = notif,
        challenge   = challengeCfg,
        meta        = SyncMeta(createdAt = now, updatedAt = now)
    )
}

private operator fun Instant.plus(dateTimePeriod: DateTimePeriod): Instant {
    return this.plus(dateTimePeriod, TimeZone.currentSystemDefault())
}

/* -------------------- Habit ➜ Form -------------------- */

/* data/mappers/HabitFormMappers.kt (fragmento) */
@RequiresApi(Build.VERSION_CODES.O)
fun Habit.toForm(): HabitForm = HabitForm(

    /* ------ Básico ------ */
    name       = name,
    desc       = description,
    category   = category,
    icon       = icon,

    /* ------ Sesión ------ */
    sessionQty  = session.qty,
    sessionUnit = session.unit,

    /* ------ Repetición ------ */
    repeatPreset = repeat.toPreset(),
    weekDays     = (repeat as? Repeat.Weekly)
        ?.days
        ?.map(DayOfWeek::getValue)
        ?.toSet()
        ?: emptySet(),

    /* ------ Periodo ------ */
    periodQty  = period.qty,
    periodUnit = period.unit,

    /* ------ Notificación ------ */
    notify          = notifConfig.enabled,
    notifMessage    = notifConfig.message,
    notifTimes      = notifConfig.times.map { it.toString() }.toSet(),
    notifAdvanceMin = notifConfig.advanceMin,
    notifMode       = notifConfig.mode,
    notifVibrate    = notifConfig.vibrate,
    notifStartsAt   = notifConfig.startsAt?.toString(),

    /* ------ Reto ------ */
    challenge = (challenge != null)
)

