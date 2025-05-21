/* data/remote/dto/FbHabitTemplate.kt */
package com.app.data.remote.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.mappers.RepeatMappers.toPreset
import com.app.data.mappers.RepeatMappers.weekDaysSet
import com.app.domain.config.NotifConfig
import com.app.domain.config.Period
import com.app.domain.config.Repeat
import com.app.domain.config.Session
import com.app.domain.entities.HabitForm
import com.app.domain.enums.HabitCategory
import kotlinx.serialization.Serializable

@Serializable
data class FbHabitTemplate(
    val name        : String              = "",
    val description : String              = "",
    val category    : HabitCategory       = HabitCategory.SALUD,
    val icon        : String              = "ic_favorite",

    /* Bloque “tracking” idéntico al de un hábito normal  */
    val session     : Session             = Session(),
    val period      : Period              = Period(),
    val repeat      : Repeat              = Repeat.None,

    /* Config de notificación completa  */
    val notifConfig : NotifConfig         = NotifConfig()
) {
    /** Genera el `HabitForm` que usa el wizard */
    @RequiresApi(Build.VERSION_CODES.O)
    fun toForm() = HabitForm(
        name          = name,
        desc          = description,
        category      = category,
        icon          = icon,

        /* tracking */
        sessionQty    = session.qty,
        sessionUnit   = session.unit,
        repeatPreset  = repeat.toPreset(),   // helpers abajo
        weekDays      = repeat.weekDaysSet(),
        periodQty     = period.qty,
        periodUnit    = period.unit,

        /* notificación */
        notify        = notifConfig.enabled,
        notifMessage  = notifConfig.message,
        notifTimes    = notifConfig.times.map { it.toString() }.toSet(),
        notifAdvanceMin = notifConfig.advanceMin
    )
}
