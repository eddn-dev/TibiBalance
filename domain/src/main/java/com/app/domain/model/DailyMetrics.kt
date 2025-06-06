package com.app.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Modelo de dominio para representar una métrica diaria.
 * Debe corresponder con los campos de DailyMetricsEntity en Room.
 */
data class DailyMetrics(
    /**
     * En el ZIP, tu entidad Room usa "date: LocalDate" como @PrimaryKey.
     * Por eso aquí el tipo es LocalDate (no Long ni String).
     */
    val date: LocalDate,

    /** El número de pasos; en la entidad Room se define como Int */
    val steps: Int,

    /**
     * En la entidad Room tu columna se llama "avgHeart: Int?"
     * (promedio de frecuencia cardíaca). Puede ser nulo.
     */
    val avgHeart: Int?,

    /**
     * En Room tu columna se llama "calories: Int?"
     * (calorías quemadas). Puede ser nulo.
     */
    val calories: Int?,

    /**
     * Tu entidad Room definió "source: String"
     * (p. ej. "wear_os" o de otro origen).
     */
    val source: String,

    /**
     * En la entidad Room, "importedAt: Instant"
     * (marca de tiempo de cuándo llegó la métrica al dispositivo).
     */
    val importedAt: Instant,

    /** El flag de sincronización, que en la entidad está dentro de SyncMeta.pendingSync */
    val pendingSync: Boolean
)
