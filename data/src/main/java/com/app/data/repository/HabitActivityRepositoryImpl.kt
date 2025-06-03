package com.app.data.repository

import com.app.data.local.dao.HabitActivityDao
import com.app.data.mappers.toDomain
import com.app.data.mappers.toEntity
import com.app.data.remote.datasource.HabitActivityRemoteDataSource
import com.app.domain.entities.HabitActivity
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import com.app.domain.repository.AuthRepository
import com.app.domain.repository.HabitActivityRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock

@Singleton
class HabitActivityRepositoryImpl @Inject constructor(
    private val dao       : HabitActivityDao,
    private val remote    : HabitActivityRemoteDataSource,
    private val authRepo  : AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : HabitActivityRepository {

    /* ──────────────── observación ──────────────── */

    override fun observeByDate(date: LocalDate): Flow<List<HabitActivity>> =
        dao.observeByDate(date)
            .map { it.toDomain() }
            .distinctUntilChanged()

    override fun observeByHabit(habitId: HabitId): Flow<List<HabitActivity>> =
        dao.observeByHabit(habitId.raw)
            .map { it.toDomain() }
            .distinctUntilChanged()

    /* ─────────────   creación / lote   ──────────── */

    override suspend fun insert(entity: HabitActivity) =
        withContext(io) { dao.insert(entity.toEntity()) }

    override suspend fun insertAll(entities: List<HabitActivity>) =
        withContext(io) { dao.insertAll(entities.map { it.toEntity() }) }

    /* ───────────── registrando progreso ─────────── */

    override suspend fun update(entity: HabitActivity) =
        withContext(io) { dao.update(entity.toEntity()) }

    override suspend fun markProgress(
        id: ActivityId,
        recordedQty: Int?,
        newStatus: ActivityStatus,
        loggedAtUtc: Instant
    ) = withContext(io) {
        val current = dao.findById(id.raw) ?: return@withContext
        val updated = current.copy(
            recordedQty = recordedQty,
            status      = newStatus,
            loggedAt    = loggedAtUtc,
            meta        = current.meta.copy(updatedAt = Clock.System.now(), pendingSync = true)
        )
        dao.update(updated)
    }

    /* ───────────────   limpieza   ──────────────── */

    override suspend fun delete(id: ActivityId) =
        withContext(io) { dao.delete(id.raw) }

    override suspend fun purgeSyncedOrDeleted() =
        withContext(io) { dao.purgeSyncedOrDeleted() }

    /* ────────────   sincronización   ───────────── */

    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = authRepo.authState().first()
            ?: return@withContext Result.failure(IllegalStateException("No user"))

        return@withContext try {

            /* 1️⃣ PUSH locales pendientes */
            dao.pendingToSync().forEach { local ->
                remote.pushActivity(uid, local.toDomain())
                dao.upsert(local.copy(meta = local.meta.copy(pendingSync = false)))
            }

            /* 2️⃣ PULL remotas + LWW */
            val remotes = remote.fetchUserActivities(uid)
            remotes.forEach { remoteAct ->
                val local = dao.findById(remoteAct.id.raw)
                val winner =
                    if (local == null || local.meta.updatedAt < remoteAct.meta.updatedAt)
                        remoteAct
                    else
                        local.toDomain()
                dao.upsert(winner.toEntity())
            }

            Result.success(Unit)

        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }
}
