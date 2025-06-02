/*
 * DTO de Habit para Firestore – versión tolerante a legacy
 */
package com.app.data.remote.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.domain.common.SyncMeta
import com.app.domain.config.ChallengeConfig
import com.app.domain.config.NotifConfig
import com.app.domain.config.Period
import com.app.domain.config.Repeat
import com.app.domain.config.Session
import com.app.domain.entities.Habit
import com.app.domain.enums.HabitCategory
import com.app.domain.enums.NotifChannel
import com.app.domain.enums.NotifMode
import com.app.domain.enums.OccurrenceInMonth
import com.app.domain.ids.HabitId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinInstant
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

/*────────────────────────  DTO principal  ────────────────────────*/
@Suppress("unused")       // necesario para Firestore
data class HabitDto(
    /* identidad */
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    /* básicos */
    var name        : String = "",
    var description : String = "",
    var category    : String = HabitCategory.SALUD.name,
    var icon        : String = "ic_favorite",

    /* configs */
    var session     : Session         = Session(),
    var repeat      : Map<String,Any> = mapOf("type" to "none"),
    var period      : Period          = Period(),
    var notifConfig : Map<String,Any> = mapOf("enabled" to false),
    var challenge   : Map<String,Any>? = null,

    /* metadatos */
    var isBuiltIn   : Boolean    = false,
    var createdAt   : Timestamp? = null,
    var updatedAt   : Timestamp? = null,
    var deletedAt   : Timestamp? = null,
    var pendingSync : Boolean    = false
) {

    /*─────────────  DTO ➜ Dominio  ─────────────*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(): Habit = Habit(
        id          = HabitId(id),
        name        = name,
        description = description,
        category    = runCatching { HabitCategory.valueOf(category) }
            .getOrElse { HabitCategory.SALUD },
        icon        = icon,
        session     = session,
        repeat      = mapToRepeat(repeat),
        period      = period,
        notifConfig = mapToNotif(notifConfig),
        challenge   = mapToChallenge(challenge),
        isBuiltIn   = isBuiltIn,
        meta        = SyncMeta(
            createdAt   = createdAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            updatedAt   = updatedAt?.toInstant()?.toKotlinInstant() ?: Instant.DISTANT_PAST,
            deletedAt   = deletedAt?.toInstant()?.toKotlinInstant(),
            pendingSync = pendingSync
        )
    )

    /*─────────────  Dominio ➜ DTO  ─────────────*/
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun fromDomain(h: Habit) = HabitDto(
            id          = h.id.raw,
            name        = h.name,
            description = h.description,
            category    = h.category.name,
            icon        = h.icon,
            session     = h.session,
            repeat      = repeatToMap(h.repeat),
            period      = h.period,
            notifConfig = notifToMap(h.notifConfig),
            challenge   = challengeToMap(h.challenge),
            isBuiltIn   = h.isBuiltIn,
            createdAt   = h.meta.createdAt.toTimestamp(),
            updatedAt   = h.meta.updatedAt.toTimestamp(),
            deletedAt   = h.meta.deletedAt.toTimestamp(),
            pendingSync = h.meta.pendingSync
        )
    }
}

/*────────────────────  Repeat ⇆ Map helpers  ────────────────────*/
@RequiresApi(Build.VERSION_CODES.O)
private fun mapToRepeat(raw: Map<String,Any>?): Repeat {
    if (raw == null) return Repeat.None

    // compat: documentos legacy con "_type"
    val tag = (raw["type"] ?: raw["_type"]) as? String ?: return Repeat.None
    val type = tag.substringAfterLast('.')   // quita paquete si venía completo

    return when (type) {
        "daily" , "Daily" -> Repeat.Daily((raw["every"] as? Number ?: 1).toInt())

        "weekly", "Weekly" -> {
            val days = (raw["days"] as? List<*>).orEmpty()
                .mapNotNull { (it as? Number)?.toInt() }
                .map { DayOfWeek.of(it) }
                .toSet()
            Repeat.Weekly(days)
        }

        "monthly" , "Monthly" -> Repeat.Monthly((raw["dayOfMonth"] as Number).toInt())

        "monthlyByWeek", "MonthlyByWeek" -> {
            val dow = DayOfWeek.of((raw["dayOfWeek"] as Number).toInt())
            val occ = OccurrenceInMonth.valueOf(raw["occurrence"] as String)
            Repeat.MonthlyByWeek(dow, occ)
        }

        "yearly" , "Yearly" -> Repeat.Yearly(
            (raw["month"] as Number).toInt(),
            (raw["day"]   as Number).toInt()
        )

        "business", "BusinessDays" -> Repeat.BusinessDays((raw["every"] as? Number ?: 1).toInt())

        else -> Repeat.None
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun repeatToMap(r: Repeat): Map<String,Any> = when (r) {
    Repeat.None -> mapOf("type" to "none")

    is Repeat.Daily -> mapOf("type" to "daily", "every" to r.every)

    is Repeat.Weekly -> mapOf(
        "type" to "weekly",
        "days" to r.days.map { it.value }               // 1…7
    )

    is Repeat.Monthly -> mapOf(
        "type" to "monthly",
        "dayOfMonth" to r.dayOfMonth
    )

    is Repeat.MonthlyByWeek -> mapOf(
        "type"       to "monthlyByWeek",
        "dayOfWeek"  to r.dayOfWeek.value,
        "occurrence" to r.occurrence.name
    )

    is Repeat.Yearly -> mapOf(
        "type"  to "yearly",
        "month" to r.month,
        "day"   to r.day
    )

    is Repeat.BusinessDays -> mapOf(
        "type"  to "business",
        "every" to r.every
    )
}

/*───────────────────  NotifConfig ⇆ Map helpers  ─────────────────*/
@RequiresApi(Build.VERSION_CODES.O)
private fun mapToNotif(raw: Map<String,Any>?): NotifConfig {
    if (raw == null) return NotifConfig(enabled = false)

    val fmt = DateTimeFormatter.ofPattern("HH:mm")

    // horas: acepta lista de strings "07:00" o mapas LocalTime anticuados
    val times = (raw["times"] as? List<*>)?.mapNotNull {
        when (it) {
            is String -> LocalTime.parse(it)
            is Map<*,*> -> {                       // legacy LocalTime serializado
                val h = (it["hour"] as? Number)?.toInt()
                val m = (it["minute"] as? Number)?.toInt()
                if (h != null && m != null) LocalTime(h, m) else null
            }
            else -> null
        }
    }.orEmpty()

    val pattern = mapToRepeat(raw["pattern"] as? Map<String,Any>)

    val starts  = (raw["startsAt"]  as? String)?.let { LocalDate.parse(it) }
    val expires = (raw["expiresAt"] as? String)?.let { LocalDate.parse(it) }

    return NotifConfig(
        enabled     = raw["enabled"] as? Boolean ?: true,
        message     = raw["message"] as? String ?: "¡Es hora!",
        times       = times,
        advanceMin  = (raw["advanceMin"] as? Number ?: 0).toInt(),
        snoozeMin   = (raw["snoozeMin"]  as? Number ?: 10).toInt(),
        mode        = runCatching { NotifMode.valueOf(raw["mode"] as? String ?: "SILENT") }
            .getOrElse { NotifMode.SILENT },
        vibrate     = raw["vibrate"] as? Boolean ?: true,
        channel     = runCatching { NotifChannel.valueOf(raw["channel"] as? String ?: "HABITS") }
            .getOrElse { NotifChannel.HABITS },
        startsAt    = starts,
        expiresAt   = expires
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun notifToMap(cfg: NotifConfig): Map<String,Any> {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    return buildMap<String,Any> {
        put("enabled",    cfg.enabled)
        put("message",    cfg.message)
        put("times",      cfg.times.map { it.toJavaLocalTime().format(fmt) })
        put("advanceMin", cfg.advanceMin)
        put("snoozeMin",  cfg.snoozeMin)
        put("mode",       cfg.mode.name)
        put("vibrate",    cfg.vibrate)
        put("channel",    cfg.channel.name)
        cfg.startsAt?.let  { put("startsAt",  it.toString()) }
        cfg.expiresAt?.let { put("expiresAt", it.toString()) }
    }
}

/*────────────────────  Ext utils  ───────────────────*/
@RequiresApi(Build.VERSION_CODES.O)
private fun Instant.toTimestamp(): Timestamp =
    Timestamp(toJavaInstant())

/*────────────────  ChallengeConfig ⇆ Map helpers  ───────────────*/
private fun mapToChallenge(raw: Map<String,Any>?): ChallengeConfig? {
    if (raw == null) return null
    return runCatching {
        ChallengeConfig(
            start         = Instant.parse(raw["start"]  as String),
            end           = Instant.parse(raw["end"]    as String),
            currentStreak = (raw["currentStreak"] as? Number ?: 0).toInt(),
            totalSessions = (raw["totalSessions"] as? Number ?: 0).toInt(),
            failed        = raw["failed"] as? Boolean ?: false,
            lastFailureAt = (raw["lastFailureAt"] as? String)?.let(Instant::parse)
        )
    }.getOrNull()
}

private fun challengeToMap(cfg: ChallengeConfig?): Map<String, Any>? =
    cfg?.let {
        buildMap {
            put("start",         it.start.toString())
            put("end",           it.end.toString())
            put("currentStreak", it.currentStreak)
            put("totalSessions", it.totalSessions)
            put("failed",        it.failed)
            it.lastFailureAt?.let { ts -> put("lastFailureAt", ts.toString()) }
        }
    }
