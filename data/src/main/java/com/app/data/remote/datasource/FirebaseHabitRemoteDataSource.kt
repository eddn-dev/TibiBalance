/**
 * @file    FirebaseHabitRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Implementación Firebase/Firestore del contrato [HabitRemoteDataSource].
 *
 * @details   Separa completamente detalles de Firestore del resto de la app.
 *  – Usa DTOs serializables (HabitDto) para los documentos.
 *  – Garantiza que ninguna excepción cruda se propague: todo se envuelve en Result.
 *  – Convierte siempre a entidades de dominio antes de exponer datos.
 *
 * Colecciones utilizadas:
 *   • users/{uid}/habits
 *   • habitTemplates
 *   • users/{uid}/habitActivities
 *
 */
package com.app.data.remote.datasource

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.remote.model.HabitActivityDto
import com.app.data.remote.model.HabitDto
import com.app.domain.config.Session
import com.app.domain.entities.Habit
import com.app.domain.entities.HabitActivity
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.HabitId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseHabitRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore,
) : HabitRemoteDataSource {

    /* ───── Herramientas privadas ───────────────────────────────────────── */

    private fun habitColl(uid: String)         = db.collection("users").document(uid).collection("habits")
    private fun activityColl(uid: String)      = db.collection("users").document(uid).collection("habitActivities")
    private val templateColl                   = db.collection("habitTemplates")

    /* ───── API pública ─────────────────────────────────────────────────── */

    /** Devuelve las plantillas como entidades de dominio. */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchTemplates(): List<Habit> = try {
        val snapshot = templateColl.get().await()
        val list = snapshot
            .toObjects(HabitDto::class.java)
            .map { it.toDomain() }

        android.util.Log.d("HabitTemplates",
            "🔥 descargadas ${list.size} plantillas: ${
                list.joinToString { it.name }
            }")

        list
    } catch (ex: Exception) {
        android.util.Log.e("HabitTemplates", "❌ error al descargar", ex)
        emptyList()
    }

    /** Descarga todos los hábitos creados por el usuario (users/{uid}/habits). */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun fetchUserHabits(uid: String): List<Habit> = runCatching {
        val snapshot = habitColl(uid).get().await()
        val list = snapshot
            .toObjects(HabitDto::class.java)
            .map { it.toDomain() }
        list
    }.getOrElse {
        android.util.Log.e("fetchUserHabits", "❌ error al descargar", it)
        emptyList()
    }

    /* ------------- PUSH ------------- */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun pushHabit(uid: String, habit: Habit) {
        habitColl(uid)
            .document(habit.id.raw)
            .set(HabitDto.fromDomain(habit), SetOptions.merge()) // ✅ usa DTO
            .await()
    }

    override suspend fun deleteHabit(uid: String, id: HabitId) {
        runCatching { habitColl(uid).document(id.raw).delete().await() }.onFailure { /* log/metrics */ }
    }

    override suspend fun pushActivity(uid: String, activity: HabitActivity) {
        runCatching {
            activityColl(uid).document(activity.id.raw)
                .set(HabitActivityDto.fromDomain(activity))
                .await()
        }
    }
}
