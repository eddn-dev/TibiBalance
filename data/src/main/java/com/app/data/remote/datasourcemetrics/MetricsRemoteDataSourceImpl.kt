// data/src/main/java/com/app/data/remote/datasourcemetrics/MetricsRemoteDataSourceImpl.kt
package com.app.data.remote.datasourcemetrics

import com.app.data.remote.datasourcemetrics.model.FirestoreMetricsDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementación de MetricsRemoteDataSource que sube métricas a Firestore.
 *
 * - Usa la colección "users/{userId}/daily_metrics/{date}".
 * - getUserId: función que debe devolver el userId actual (o null si no hay sesión).
 */
class MetricsRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val getUserId: () -> String?
) : MetricsRemoteDataSource {

    override suspend fun uploadMetric(dto: FirestoreMetricsDto) {
        // 1) Obtener userId (puede retornar null si no hay sesión)
        val userId = getUserId()
            ?: throw IllegalStateException("No hay usuario autenticado para subir métricas")

        // 2) Construir la ruta: users/{userId}/daily_metrics/{date}
        val docRef = firestore
            .collection("users")
            .document(userId)
            .collection("daily_metrics")
            .document(dto.date)

        // 3) Convertir el DTO a un mapa para Firestore
        val data = mapOf(
            "date" to dto.date,
            "steps" to dto.steps,
            "avgHeart" to dto.avgHeart,
            "calories" to dto.calories,
            "source" to dto.source,
            "importedAtEpoch" to dto.importedAtEpoch
        )

        // 4) Hacer set() en Firestore y esperar a que termine
        docRef.set(data).await()
    }
}
