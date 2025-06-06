// data/src/main/java/com/app/data/remote/datasourcemetrics/model/FirestoreMetricsDto.kt
package com.app.data.remote.datasourcemetrics.model

/**
 * DTO que representa la métrica diaria tal como la guardaremos en Firestore.
 *
 * En Firestore, la ruta será: users/{userId}/daily_metrics/{date}.json
 *
 * - date: String (“2025-06-05”), a partir de LocalDate.toString().
 * - steps, avgHeart, calories, source: mismos que en tu entidad/domino.
 * - importedAtEpoch: Long (epoch ms) para reconstruir el Instant.
 */
data class FirestoreMetricsDto(
    val date: String,
    val steps: Int,
    val avgHeart: Int?,
    val calories: Int?,
    val source: String,
    val importedAtEpoch: Long
)
