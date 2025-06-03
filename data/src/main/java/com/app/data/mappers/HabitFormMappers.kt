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

@RequiresApi(Build.VERSION_CODES.O)
fun HabitForm.toHabit(
    id : HabitId = HabitId(UUID.randomUUID().toString()),
    now: Instant = Clock.System.now()
): Habit {

    /* ────── sesión / repetición / periodo ────── */
    val session = Session(sessionQty, sessionUnit)
    val repeat  = repeatPreset.toRepeat(weekDays)
    val period  = Period(periodQty, periodUnit)

    /* ────── reto ────── */
    val startDay = now.atStartOfDaySystem()
    val challengeCfg: ChallengeConfig? =
        if (challenge) {
            val end = when {
                periodQty != null && periodUnit != PeriodUnit.INDEFINIDO ->
                    startDay.plus(periodQty!!, periodUnit)
                else -> startDay + DateTimePeriod(days = 21)        // ← fallback 3 semanas
            }

            ChallengeConfig(
                start         = startDay,
                end           = end,
                currentStreak = 0,
                totalSessions = 0
            )
        } else null

    /* ────── notificaciones ────── */
    val expiresAt: LocalDate? =
        challengeCfg?.end?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
            ?: when {
                periodQty != null && periodUnit != PeriodUnit.INDEFINIDO ->
                    startDay.plus(periodQty!!, periodUnit)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                else -> null
            }

    val notif = NotifConfig(
        enabled     = notify,                               // ← sólo se conmuta el flag
        message     = notifMessage.ifBlank { "¡Es hora!" },
        times       = notifTimes.map(LocalTime::parse),
        advanceMin  = notifAdvanceMin,
        snoozeMin   = notifSnoozeMin,
        repeatQty   = notifRepeatQty,
        mode        = notifMode,
        vibrate     = notifVibrate,
        startsAt    = notifStartsAt?.let(LocalDate::parse),
        expiresAt   = expiresAt,
        channel     = NotifChannel.HABITS,                  // o tu valor por defecto
    )

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

/* helpers --------------------------------------------------- */
private fun Instant.plus(qty: Int, unit: PeriodUnit): Instant = when (unit) {
    PeriodUnit.DIAS      -> this + DateTimePeriod(days   = qty)
    PeriodUnit.SEMANAS   -> this + DateTimePeriod(days   = qty * 7)
    PeriodUnit.MESES     -> this + DateTimePeriod(months = qty)
    PeriodUnit.INDEFINIDO-> this
}

private operator fun Instant.plus(p: DateTimePeriod): Instant =
    this.plus(p, TimeZone.currentSystemDefault())

private fun Instant.atStartOfDaySystem(): Instant =
    toLocalDateTime(TimeZone.currentSystemDefault())
        .date.atStartOfDayIn(TimeZone.currentSystemDefault())


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
    notifRepeatQty  = notifConfig.repeatQty,
    notifSnoozeMin  = notifConfig.snoozeMin,
    notifMode       = notifConfig.mode,
    notifVibrate    = notifConfig.vibrate,
    notifStartsAt   = notifConfig.startsAt?.toString(),

    /* ------ Reto ------ */
    challenge = (challenge != null)
)

