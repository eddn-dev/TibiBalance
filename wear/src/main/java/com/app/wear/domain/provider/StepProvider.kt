// :wear/domain/provider/StepProvider.kt
package com.app.wear.domain.provider

interface StepProvider {
    /** Pasos acumulados desde el inicio del día (timezone local). */
    suspend fun todaySteps(): Int
}
