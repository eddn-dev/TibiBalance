/* data/remote/firebase/HabitTemplateService.kt */
package com.app.data.remote.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.mappers.JsonConfig
import com.app.data.remote.model.FbHabitTemplate
import com.app.data.remote.utils.asJson
import com.app.domain.entities.HabitTemplate
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HabitTemplateService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val json: Json = JsonConfig.default          // misma instancia global
) {
    private val col get() = firestore.collection("habitTemplates")

    /* ---------- fetch puntual ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchOnce(): List<HabitTemplate> =
        col.get().await().documents.mapNotNull { it.safeToTemplate() }

    /* ---------- listener tiempo-real ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun observe(): Flow<List<HabitTemplate>> = callbackFlow {
        val reg = col.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { it.safeToTemplate() }.orEmpty())
        }
        awaitClose { reg.remove() }
    }

    /* ---------- mapper central ---------- */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun DocumentSnapshot.safeToTemplate(): HabitTemplate? = runCatching {
        val dto   = json.decodeFromJsonElement(
            FbHabitTemplate.serializer(),
            (data ?: return null).asJson()
        )
        HabitTemplate(
            id        = id,
            name      = dto.name,
            icon      = dto.icon,
            category  = dto.category,
            formDraft = dto.toForm()
        )
    }.getOrNull()
}
