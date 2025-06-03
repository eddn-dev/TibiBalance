package com.app.data.remote.datasource

import com.app.domain.entities.HabitActivity
import com.app.domain.ids.ActivityId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Instant

/**
 * Implementación sencilla: serializamos el objeto completo a JSON
 * y lo guardamos en el campo "data".
 *
 *  Ventajas:
 *    • No dependemos de los tipos nativos que entiende Firestore.
 *    • Los cambios de esquema sólo requieren migrar localmente.
 *
 *  Campos guardados:
 *    "data"      -> Json del objeto HabitActivity.
 *    "updatedAt" -> Epoch millis (para LWW).
 */
@Singleton
class FirebaseHabitActivityRemoteDataSource @Inject constructor(
    private val db  : FirebaseFirestore,
    private val json: Json            // provisto por SerializationModule
) : HabitActivityRemoteDataSource {

    /* helpers ---------------------------------------------------- */

    /** users/{uid}/activities */
    private fun col(uid: String) =
        db.collection("users").document(uid).collection("activities")

    private fun Instant?.epochMs(): Long =
        this?.toEpochMilliseconds() ?: 0L

    /* ------------- interfaz ------------------------------------ */

    override suspend fun pushActivity(uid: String, activity: HabitActivity) {
        val docId = activity.id.raw
        val payload = mapOf(
            "data"      to json.encodeToString(activity),
            "updatedAt" to activity.meta.updatedAt.epochMs()
        )
        col(uid).document(docId)
            .set(payload, SetOptions.merge())
            .await()
    }

    override suspend fun deleteActivity(uid: String, id: ActivityId) {
        col(uid).document(id.raw).delete().await()
    }

    override suspend fun fetchUserActivities(uid: String): List<HabitActivity> =
        col(uid).get().await().documents.mapNotNull { snap ->
            snap.getString("data")?.let { jsonStr ->
                runCatching { json.decodeFromString<HabitActivity>(jsonStr) }.getOrNull()
            }
        }
}
