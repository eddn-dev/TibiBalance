/**
 * @file    FirestoreHabitRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Implementación Firestore de [HabitRemoteDataSource].
 */
package com.app.data.remote.datasource

import com.app.data.remote.model.HabitDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreHabitRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val uidProvider: () -> String          // Inyectado desde AuthModule
) : HabitRemoteDataSource {

    /* ----- Helpers ----- */
    private val habitsCol
        get() = firestore.collection("users")
            .document(uidProvider())
            .collection("habits")

    /* ----- Live updates ----- */
    override fun listenHabits(): Flow<HabitDto> = callbackFlow {
        val reg = habitsCol.addSnapshotListener { snap, _ ->
            snap?.documentChanges?.forEach { dc ->
                dc.document.toObject(HabitDto::class.java)?.let(::trySend)
            }
        }
        awaitClose { reg.remove() }
    }

    /* ----- Push / Pull / Delete ----- */
    override suspend fun pushHabit(dto: HabitDto) {
        habitsCol.document(dto.id).set(dto, SetOptions.merge()).await()
    }

    override suspend fun pullHabits(): List<HabitDto> =
        habitsCol.get().await().documents.mapNotNull { it.toObject(HabitDto::class.java) }

    override suspend fun deleteHabit(id: String) {
        habitsCol.document(id).delete().await()
    }
}
