/**
 * @file    HabitRepositoryImpl.kt
 * @ingroup data_repository_impl
 * @brief   Repositorio offline-first para hábitos y su actividad.
 *
 *  • Room  ← fuente reactiva y caché.
 *  • Firestore → sincronización (push / seed de templates).
 *  • AuthRepository ⇒ UID actual.
 *
 *  Colecciones remotas:
 *    • habitTemplates                       (read-only, sugestiones)
 *    • users/{uid}/habits                   (CRUD usuario)
 *    • users/{uid}/habitActivities          (tracking completado)
 */
package com.app.data.repository

import com.app.data.local.dao.HabitDao
import com.app.data.local.entities.*
import com.app.data.mappers.HabitActivityMappers.toEntity
import com.app.data.mappers.toDomain
import com.app.data.mappers.toEntity
import com.app.data.remote.datasource.HabitRemoteDataSource
import com.app.domain.common.SyncMeta
import com.app.domain.entities.*
import com.app.domain.enums.ActivityType
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import com.app.domain.repository.AuthRepository
import com.app.domain.repository.HabitRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val dao        : HabitDao,
    private val remote     : HabitRemoteDataSource,
    private val authRepo   : AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : HabitRepository {

    /* ───────────────────────── flujos  ───────────────────────── */

    override fun observeUserHabits(): Flow<List<Habit>> =
        dao.observeByBuiltIn(false)
            .map   { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun observeSuggestedHabits(): Flow<List<Habit>> =
        flow {
            /* 1) seed remoto → cache local */
            val templates = try { remote.fetchTemplates() } catch (_: Exception) { emptyList() }
            if (templates.isNotEmpty()) {
                dao.upsert( templates.map { it.toEntity() } )
            }
            /* 2) emite Room */
            emitAll(
                dao.observeByBuiltIn(true)
                    .map { it.map { e -> e.toDomain() } }
            )
        }.distinctUntilChanged()

    /* ─────────────────────── operaciones CRUD ────────────────── */

    override suspend fun create(habit: Habit) = withContext(io) {
        val uid = currentUid() ?: return@withContext
        val entity = habit.copy(
            meta = habit.meta.copy(createdAt = Clock.System.now(), pendingSync = true)
        ).toEntity()

        dao.upsert(entity)
        try {
            remote.pushHabit(uid, habit)
            dao.upsert(entity.copy(meta = entity.meta.copy(pendingSync = false)))
        } catch (_: Exception) { /* se queda pendingSync = true */ }
    }

    override suspend fun update(habit: Habit) = withContext(io) {
        require(!habit.isBuiltIn) { "Plantillas sugeridas no son editables" }
        val uid = currentUid() ?: return@withContext

        val updated = habit.copy(
            meta = habit.meta.copy(updatedAt = Clock.System.now(), pendingSync = true)
        )
        dao.upsert(updated.toEntity())

        try {
            remote.pushHabit(uid, updated)
            dao.upsert(updated.copy(meta = updated.meta.copy(pendingSync = false)).toEntity())
        } catch (_: Exception) { /* pendingSync queda true */ }
    }

    override suspend fun delete(id: HabitId) = withContext(io) {
        val uid = currentUid() ?: return@withContext
        dao.delete(id.raw)
        try { remote.deleteHabit(uid, id) } catch (_: Exception) { /* ignorar, worker */ }
    }

    /* ──────────────── registrar actividad / completado ───────── */

    override suspend fun markCompleted(id: HabitId, at: Instant) = withContext(io) {
        val uid = currentUid() ?: return@withContext
        val act = HabitActivity(
            id        = ActivityId("${id.raw}@$at"),
            habitId   = id,
            completedAt = at,
            meta      = SyncMeta(pendingSync = true)
        )

        dao.insertActivity(act.toEntity())

        try {
            remote.pushActivity(uid, act)
            dao.insertActivity(act.copy(meta = act.meta.copy(pendingSync = false)).toEntity())
        } catch (_: Exception) { /* offline, worker reintentará */ }
    }

    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = currentUid() ?: return@withContext Result.failure(IllegalStateException("No user"))

        return@withContext try {

            /* 1) PUSH locales pendientes */
            dao.pendingToSync().forEach { entity ->
                remote.pushHabit(uid, entity.toDomain())
                dao.upsert(entity.copy(meta = entity.meta.copy(pendingSync = false)))
            }

            /* 2) PULL remotos y resolver LWW */
            val remotes = remote.fetchUserHabits(uid)
            remotes.forEach { remoteHabit ->
                val local = dao.findById(remoteHabit.id.raw)          // suspend OK aquí
                val winner = resolveLww(local, remoteHabit)
                dao.upsert(winner.toEntity())
            }

            Result.success(Unit)

        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }

    /* ───────────────────────── helpers ───────────────────────── */

    private suspend fun currentUid(): String? =
        authRepo.authState().first()

    /**
     * Resuelve conflicto Last-Write-Wins entre local (puede ser null) y remoto.
     */
    private fun resolveLww(
        localEntity: HabitEntity?,
        remoteHabit: Habit
    ): Habit {
        val local = localEntity?.toDomain()
        return when {
            local == null -> remoteHabit
            local.meta.updatedAt >= remoteHabit.meta.updatedAt -> local
            else -> remoteHabit
        }
    }

}