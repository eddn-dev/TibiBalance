/* ui/screens/habits/addHabitWizard/HabitFormSaver.kt */
package com.app.tibibalance.ui.screens.habits.addHabitWizard

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import com.app.domain.config.RepeatPreset
import com.app.domain.entities.HabitForm
import com.app.domain.enums.HabitCategory
import com.app.domain.enums.PeriodUnit
import com.app.domain.enums.SessionUnit

/**
 * Saver que permite a `rememberSaveable` conservar un [HabitForm] al atravesar
 * recreaciones de la Activity/Proceso (cambios de orientación, killing-process, etc.).
 *
 * Serializa el formulario a un `Map<String, Any?>` con únicamente tipos
 * primitivos admitidos por Compose (String, Int, Boolean, List, etc.).
 */
val HabitFormSaver: Saver<HabitForm, Any> = mapSaver(
    save = { f ->
        mapOf(
            "name"         to f.name,
            "desc"         to f.desc,
            "icon"         to f.icon,
            "category"     to f.category.ordinal,
            "sessionQty"   to f.sessionQty,
            "sessionUnit"  to f.sessionUnit.ordinal,
            "repeat"       to f.repeatPreset.ordinal,
            "weekDays"     to f.weekDays.toList(),     // List<Int>
            "periodQty"    to f.periodQty,
            "periodUnit"   to f.periodUnit.ordinal,
            "notify"       to f.notify,
            "challenge"    to f.challenge,
            "notifMessage" to f.notifMessage,
            "notifTimes"   to f.notifTimes.toList()    // List<String>
        )
    },
    restore = { m ->
        HabitForm(
            name         = m["name"]        as String,
            desc         = m["desc"]        as String,
            icon         = m["icon"]        as String,
            category     = HabitCategory.entries[(m["category"] as Int)],
            sessionQty   = m["sessionQty"]  as Int?,
            sessionUnit  = SessionUnit.entries[(m["sessionUnit"] as Int)],
            repeatPreset = RepeatPreset.entries[(m["repeat"] as Int)],
            weekDays     = (m["weekDays"] as List<Int>).toSet(),
            periodQty    = m["periodQty"]   as Int?,
            periodUnit   = PeriodUnit.entries[(m["periodUnit"] as Int)],
            notify       = m["notify"]      as Boolean,
            challenge    = m["challenge"]   as Boolean,
            notifMessage = m["notifMessage"] as String,
            notifTimes   = (m["notifTimes"] as List<String>).toSet()

        )
    }
)
