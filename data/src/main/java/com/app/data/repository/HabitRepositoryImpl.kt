package com.app.data.repository

import com.app.data.local.dao.HabitDao
import com.app.data.mappers.HabitMappers.toDomain
import com.app.data.mappers.HabitMappers.toEntity
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/* ── Dispatcher qualifier ───────────────────────────── */
@Qualifier annotation class IoDispatcher

/* ── Implementación singleton ───────────────────────── */
@OptIn(DelicateCoroutinesApi::class)
@Singleton                      // ✅ usa scope oficial de Hilt
class HabitRepositoryImpl @Inject constructor(
    private val habitDao : HabitDao,
    private val firestore: FirebaseFirestore,
    @IoDispatcher private val io: CoroutineDispatcher,
    /** Devuelve UID actual – inyectado desde AuthModule */
    private val uidProvider: () -> String
) : HabitRepository {

    /* ───────── helpers Firestore ───────── */
    private val habitsCol
        get() = firestore.collection("users")
            .document(uidProvider())
            .collection("habits")

    /* ───────── API pública ─────────────── */
    override fun getHabitsFlow(): Flow<List<Habit>> =
        habitDao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun createHabit(habit: Habit): Result<Unit> =
        runCatchingIO {
            val now = Clock.System.now()
            habitDao.upsertAll(
                habit.copy(meta = habit.meta.copy(
                    createdAt   = now,
                    updatedAt   = now,
                    pendingSync = true
                )).toEntity()
            )
        }

    override suspend fun updateHabit(habit: Habit): Result<Unit> =
        runCatchingIO {
            habitDao.upsertAll(
                habit.copy(meta = habit.meta.copy(
                    updatedAt   = Clock.System.now(),
                    pendingSync = true
                )).toEntity()
            )
        }

    override suspend fun deleteHabit(id: HabitId, hard: Boolean): Result<Unit> =
        runCatchingIO {
            if (hard) {
                habitDao.deleteById(id.value)            // DAO ya creado
            } else {
                habitDao.softDelete(
                    id.value,
                    Clock.System.now().toEpochMilliseconds()
                )
            }
        }

    override suspend fun markCompleted(
        id: HabitId,
        at: Instant
    ): Result<Unit> = runCatchingIO {
        habitDao.touchForCompletion(id.value, at.toEpochMilliseconds())
        /* insertar HabitActivityEntity ➜ TODO en fase actividades */
    }

    override suspend fun syncNow(): Result<Unit> =
        runCatchingIO {
            pushLocalPending()
            pullRemoteChanges()
        }

    /* ───────── private helpers ─────────── */

    private suspend fun pushLocalPending() = withContext(io) {
        habitDao.pendingSyncSnapshot().forEach { entity ->
            habitsCol.document(entity.id.value)
                .set(entity, SetOptions.merge())
                .await()
            habitDao.clearPending(entity.id.value)
        }
    }

    private suspend fun pullRemoteChanges() = withContext(io) {
        habitsCol.get().await().documents.forEach { doc ->
            val remote =
                doc.toObject(com.app.data.local.entities.HabitEntity::class.java)
                    ?: return@forEach
            val local = habitDao.findByIdSync(remote.id.value)
            if (local == null || remote.meta.updatedAt > local.meta.updatedAt) {
                habitDao.upsertAll(remote)
            }
        }
    }

    init {
        habitsCol.addSnapshotListener { snap, _ ->
            snap?.documentChanges?.forEach { change ->
                change.document
                    .toObject(com.app.data.local.entities.HabitEntity::class.java)
                    .let { entity ->
                        GlobalScope.launch(io) { habitDao.upsertAll(entity) }
                    }
            }
        }
    }

    private suspend inline fun <T> runCatchingIO(
        crossinline block: suspend () -> T
    ): Result<T> = runCatching { withContext(io) { block() } }
}
