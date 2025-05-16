// com/app/domain/config/Repeat.kt
package com.app.domain.config

import com.app.domain.enums.OccurrenceInMonth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Jerarquía polimórfica serializable de patrones de repetición.  :contentReference[oaicite:2]{index=2}
 */
@Serializable
sealed interface Repeat {
    @Serializable @SerialName("none") data object None : Repeat
    @Serializable data class Daily(val every: Int = 1) : Repeat
    @Serializable data class Weekly(val days: Set<DayOfWeek>) : Repeat
    @Serializable data class Monthly(val dayOfMonth: Int) : Repeat
    @Serializable
    data class MonthlyByWeek(
        val dayOfWeek : DayOfWeek,
        val occurrence: OccurrenceInMonth
    ) : Repeat
    @Serializable data class Yearly(val month: Int, val day: Int) : Repeat
    @Serializable data class BusinessDays(val every: Int = 1) : Repeat
}
