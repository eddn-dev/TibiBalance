// com/app/domain/config/RepeatPreset.kt
package com.app.domain.config

import kotlinx.serialization.Serializable

@Serializable
enum class RepeatPreset {
    INDEFINIDO, DIARIO, CADA_3_DIAS, CADA_SEMANA, CADA_15_DIAS,
    MENSUAL, ULTIMO_VIERNES_MES, DIAS_LABORALES, PERSONALIZADO
}
