package com.app.data.metrics.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fuente de datos remoto para métricas de usuario en Firestore.
 */
class FirebaseUserMetricsRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun collection(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("metrics")

    /** Sincroniza un lote de métricas (batch). */
    suspend fun pushAll(list: List<UserMetricsDto>) {
        if (list.isEmpty()) return
        val batch = firestore.batch()
        list.forEach { dto ->
            val doc = collection(dto.userId).document(dto.date.toString())
            batch.set(doc, dto)
        }
        batch.commit().await()
    }

    /** Actualiza una métrica individual. */
    suspend fun update(dto: UserMetricsDto) {
        collection(dto.userId)
            .document(dto.date.toString())
            .set(dto)
            .await()
    }

    /** Elimina una métrica por fecha. */
    suspend fun delete(userId: String, date: String) {
        collection(userId).document(date).delete().await()
    }
}
