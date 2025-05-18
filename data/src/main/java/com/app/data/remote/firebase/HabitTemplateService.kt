package com.app.data.remote.firebase

import com.app.data.mappers.JsonConfig
import com.app.domain.entities.HabitTemplate
import com.app.domain.enums.HabitCategory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

class HabitTemplateService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val json: Json = JsonConfig.default
) {
    private val col get() = firestore.collection("habitTemplates")

    suspend fun fetchOnce(): List<HabitTemplate> =
        col.get().await().documents.map { it.toTemplate(json) }

    fun observe(): Flow<List<HabitTemplate>> = callbackFlow {
        val reg = col.addSnapshotListener { qs, _ ->
            trySend(qs?.documents?.map { it.toTemplate(json) }.orEmpty())
        }
        awaitClose { reg.remove() }
    }

    private fun DocumentSnapshot.toTemplate(json: Json) =
        HabitTemplate(
            id        = id,
            name      = getString("name")!!,
            category  = HabitCategory.valueOf(getString("category")!!),
            icon      = getString("icon")!!,
            formDraft = json.decodeFromString(getString("formDraft")!!)
        )
}
