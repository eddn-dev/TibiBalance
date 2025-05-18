package com.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.local.dao.HabitDao
import com.app.data.mappers.HabitMappers.toDomain
import com.app.data.mappers.HabitMappers.toDto
import com.app.data.mappers.HabitMappers.toEntity
import com.app.data.remote.datasource.HabitRemoteDataSource
import com.app.domain.entities.Habit
import com.app.domain.ids.HabitId
import com.app.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/* ── Implementación singleton ───────────────────────── */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(DelicateCoroutinesApi::class)
@Singleton                      // ✅ usa scope oficial de Hilt
class HabitRepositoryImpl @Inject constructor(
    private val habitDao : HabitDao,
    private val remote: HabitRemoteDataSource,
    @IoDispatcher private val io: CoroutineDispatcher,
    /** Devuelve UID actual – inyectado desde AuthModule */
    private val uidProvider: () -> String
) : HabitRepository {

    private val syncScope by lazy { CoroutineScope(io + SupervisorJob()) }

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

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncNow(): Result<Unit> =
        runCatchingIO {
            pushLocalPending()
            pullRemoteChanges()
        }

    /* ───────── private helpers ─────────── */

    private suspend fun pushLocalPending() = withContext(io) {
        habitDao.pendingSyncSnapshot().forEach { entity ->
            remote.pushHabit(entity.toDto())               // extension toDto()
            habitDao.clearPending(entity.id.value)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun pullRemoteChanges() = withContext(io) {
        remote.pullHabits().forEach { dto ->
            val remoteE = dto.toEntity()
            val local   = habitDao.findByIdSync(remoteE.id.value)
            if (local == null || remoteE.meta.updatedAt > local.meta.updatedAt)
                habitDao.upsertAll(remoteE)
        }
    }

    init {
        subscribeToRemote()
    }

    private fun subscribeToRemote() {
        remote.listenHabits()
            .map    { it.toEntity() }
            .flowOn(io)
            .onEach { entity -> habitDao.upsertAll(entity) }
            .launchIn(syncScope)
    }


    private suspend inline fun <T> runCatchingIO(
        crossinline block: suspend () -> T
    ): Result<T> = runCatching { withContext(io) { block() } }
}
