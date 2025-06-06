// data/src/main/java/com/app/data/remote/datasourcemetrics/MetricsRemoteDataSource.kt
package com.app.data.remote.datasourcemetrics

import com.app.data.remote.datasourcemetrics.model.FirestoreMetricsDto

/**
 * DataSource remoto para subir métricas diarias a Firestore.
 *
 * Definimos solo un metodo que recibe un DTO ya “listo” para enviarse.
 * De esta forma, separamos la lógica de red (Firebase) de la lógica de dominio.
 */
interface MetricsRemoteDataSource {
    /**
     * Sube una métrica individual a Firestore, dentro de la colección
     * `users/{userId}/daily_metrics/{date}`.
     * Supone que el DTO ya trae los campos exactos que queremos guardar.
     *
     * @param dto Estructura de datos (date, steps, avgHeart, calories, source, importedAtEpoch).
     */
    suspend fun uploadMetric(dto: FirestoreMetricsDto)
}