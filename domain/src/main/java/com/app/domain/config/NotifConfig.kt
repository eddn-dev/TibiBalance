// com/app/domain/config/NotifConfig.kt
package com.app.domain.config

import com.app.domain.enums.NotifChannel
import com.app.domain.enums.NotifMode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class NotifConfig(
    val enabled    : Boolean = true,
    val message    : String  = "¡Es hora!",
    val times      : List<LocalTime> = listOf(LocalTime(20, 0)),
    val advanceMin : Int = 0,
    val snoozeMin  : Int = 10,
    val repeatQty  : Int = 0,
    val mode       : NotifMode = NotifMode.SOUND,
    val vibrate    : Boolean = true,
    val channel    : NotifChannel = NotifChannel.HABITS,
    val startsAt   : LocalDate? = null,
    val expiresAt  : LocalDate? = null
)
