// com/app/domain/config/Period.kt
package com.app.domain.config

import com.app.domain.enums.PeriodUnit
import kotlinx.serialization.Serializable

@Serializable
data class Period(val qty: Int? = null, val unit: PeriodUnit = PeriodUnit.INDEFINIDO)
