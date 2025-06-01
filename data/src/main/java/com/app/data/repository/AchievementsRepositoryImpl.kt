package com.app.data.repository

import com.app.domain.repository.AchievementsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AchievementsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AchievementsRepository {

    override suspend fun initializeAchievementsIfMissing(userId: String) {
        val logrosIniciales = listOf(
            mapOf(
                "id" to "foto_perfil",
                "name" to "Un placer conocernos",
                "description" to "Cambia tu foto de perfil."
            ),
            mapOf(
                "id" to "tibio_salud",
                "name" to "Tibio saludable",
                "description" to "Agrega un hábito de salud."
            ),
            mapOf(
                "id" to "tibio_productividad",
                "name" to "Tibio productivo",
                "description" to "Agrega un hábito de productividad."
            ),
            mapOf(
                "id" to "tibio_bienestar",
                "name" to "Tibio del bienestar",
                "description" to "Agrega un hábito de bienestar."
            ),
            mapOf(
                "id" to "primer_habito",
                "name" to "El inicio del reto",
                "description" to "Agrega tu primer hábito con modo reto activado."
            ),
            mapOf(
                "id" to "cinco_habitos",
                "name" to "La sendera del reto",
                "description" to "Agrega cinco hábitos con modo reto activado."
            ),
            mapOf(
                "id" to "feliz_7_dias",
                "name" to "Todo en su lugar",
                "description" to "Registra un estado de ánimo “feliz” por siete días consecutivos."
            ),
            mapOf(
                "id" to "emociones_30_dias",
                "name" to "Un tibio emocional",
                "description" to "Registra tus emociones por 30 días consecutivos."
            ),
            mapOf(
                "id" to "noti_personalizada",
                "name" to "¡Ya es hora!",
                "description" to "Descubriste la personalización de notificaciones desde configuración."
            )
        )

        val logrosRef = firestore.collection("users").document(userId).collection("achievements")

        val snapshot = logrosRef.get().await()
        val existentes = snapshot.documents.map { it.id }.toSet()

        logrosIniciales.forEach { logro ->
            val id = logro["id"] as String
            if (!existentes.contains(id)) {
                val data = mapOf(
                    "name" to logro["name"],
                    "description" to logro["description"],
                    "progress" to 0,
                    "unlock" to false,
                    "unlockDate" to null
                )
                logrosRef.document(id).set(data)
            }
        }
    }

    override suspend fun unlockIfNotYet(userId: String, achievementId: String): Boolean {
        val ref = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .document(achievementId)

        val snapshot = ref.get().await()
        if (snapshot.exists() && snapshot.getBoolean("unlock") != true) {
            ref.update(
                mapOf(
                    "unlock" to true,
                    "unlockDate" to com.google.firebase.Timestamp.now(),
                    "progress" to 100
                )
            ).await()
            return true
        }
        return false
    }

    override suspend fun updateProgress(userId: String, achievementId: String, progress: Int) {
        // Guarda el progreso en Firestore, Room, etc.
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("achievements")
            .document(achievementId)

        docRef.set(mapOf("progress" to progress), SetOptions.merge())
    }
}
