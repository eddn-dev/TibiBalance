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

import com.app.data.local.dao.HabitActivityDao // Added import
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
import kotlinx.datetime.DateTimeUnit // Added import
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate // Added import
import kotlinx.datetime.TimeZone // Added import
import kotlinx.datetime.atStartOfDayIn // Added import
import kotlinx.datetime.plus // Added import


@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao    : HabitDao, // Renamed for clarity
    private val activityDao : HabitActivityDao, // Added
    private val remote      : HabitRemoteDataSource,
    private val authRepo    : AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : HabitRepository {

    /* ───────────────────────── flujos  ───────────────────────── */

    override fun observeUserHabits(): Flow<List<Habit>> =
        dao.observeByBuiltIn(false)
            .map   { list -> list.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun observeSuggestedHabits(): Flow<List<Habit>> =
        flow {
            /* 1️⃣ descargar – pero en IO */
            withContext(io) {
                runCatching { remote.fetchTemplates() }
                    .getOrElse { emptyList() }
                    .takeIf { it.isNotEmpty() }
                    ?.let { templates ->
                        dao.upsert(
                            templates.map { it.copy(isBuiltIn = true).toEntity() }
                        )
                    }
            }

            /* 2️⃣ emitir Room */
            emitAll(
                dao.observeByBuiltIn(true)
                    .map { list -> list.map { it.toDomain() } }
            )
        }
            .flowOn(io)
            .distinctUntilChanged()

    override fun observeHabit(id: HabitId): Flow<Habit?> =
        habitDao.observeById(id.raw) // Changed dao to habitDao
            .map { it?.toDomain() }
            .distinctUntilChanged()

    /* ─────────────────────── One-shot reads ─────────────────── */

    override suspend fun getHabitsOnce(): List<Habit> = withContext(io) {
        try {
            // Assuming HabitDao gets a method like getAllUserHabitsList() that returns List<HabitEntity>
            // This method should filter by isBuiltIn = false
            // This will be added in Step 10: @Query("SELECT * FROM habits WHERE isBuiltIn = 0") suspend fun getAllUserHabitsList(): List<HabitEntity>
            habitDao.getAllUserHabitsList().map { it.toDomain() }
        } catch (e: Exception) {
            // Log.e("HabitRepositoryImpl", "Error fetching habits once", e) // Consider logging
            emptyList()
        }
    }

    override suspend fun getHabitByIdOnce(id: HabitId): Habit? = withContext(io) {
        try {
            habitDao.findById(id.raw)?.toDomain()
        } catch (e: Exception) {
            // Log.e("HabitRepositoryImpl", "Error fetching habit by ID once", e) // Consider logging
            null
        }
    }

    override suspend fun getHabitActivitiesByDate(localDate: kotlinx.datetime.LocalDate): List<HabitActivity> = withContext(io) {
        val systemZone = TimeZone.currentSystemDefault()
        val startOfDayInstant = localDate.atStartOfDayIn(systemZone)
        val endOfDayInstant = localDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(systemZone)

        // Using epoch milliseconds as DAOs often work with Long for date-time.
        // The DAO method will need to be compatible with this.
        // Step 10 will add:
        // @Query("SELECT * FROM activities WHERE completedAt >= :startOfDayMillis AND completedAt < :endOfDayMillis")
        // suspend fun getActivitiesForDayRangeMillis(startOfDayMillis: Long, endOfDayMillis: Long): List<HabitActivityEntity>
        // For HabitActivityDao

        val startOfDayMillis = startOfDayInstant.toEpochMilliseconds()
        val endOfDayMillis = endOfDayInstant.toEpochMilliseconds()

        try {
            activityDao.getActivitiesForDayRangeMillis(startOfDayMillis, endOfDayMillis).map { it.toDomain() }
        } catch (e: Exception) {
            // Log.e("HabitRepositoryImpl", "Error fetching activities by date", e) // Consider logging
            emptyList()
        }
    }

    /* ─────────────────────── operaciones CRUD ────────────────── */

    override suspend fun create(habit: Habit) = withContext(io) {
        val uid = currentUid() ?: return@withContext
        val entity = habit.copy(
            meta = habit.meta.copy(createdAt = Clock.System.now(), pendingSync = true)
        ).toEntity()

        habitDao.upsert(entity) // Changed dao to habitDao
        try {
            remote.pushHabit(uid, habit)
            habitDao.upsert(entity.copy(meta = entity.meta.copy(pendingSync = false))) // Changed dao to habitDao
        } catch (_: Exception) { /* se queda pendingSync = true */ }
    }

    override suspend fun update(habit: Habit) = withContext(io) {
        require(!habit.isBuiltIn) { "Plantillas sugeridas no son editables" }
        val uid = currentUid() ?: return@withContext

        val updated = habit.copy(
            meta = habit.meta.copy(updatedAt = Clock.System.now(), pendingSync = true)
        )
        habitDao.upsert(updated.toEntity()) // Changed dao to habitDao

        try {
            remote.pushHabit(uid, updated)
            habitDao.upsert(updated.copy(meta = updated.meta.copy(pendingSync = false)).toEntity()) // Changed dao to habitDao
        } catch (_: Exception) { /* pendingSync queda true */ }
    }

    override suspend fun delete(id: HabitId) = withContext(io) {
        val uid = currentUid() ?: return@withContext
        habitDao.delete(id.raw) // Changed dao to habitDao
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

        habitDao.insertActivity(act.toEntity()) // Changed dao to habitDao

        try {
            remote.pushActivity(uid, act)
            habitDao.insertActivity(act.copy(meta = act.meta.copy(pendingSync = false)).toEntity()) // Changed dao to habitDao
        } catch (_: Exception) { /* offline, worker reintentará */ }
    }

    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = currentUid() ?: return@withContext Result.failure(IllegalStateException("No user"))

        return@withContext try {

            /* 1) PUSH locales pendientes */
            habitDao.pendingToSync().forEach { entity -> // Changed dao to habitDao
                remote.pushHabit(uid, entity.toDomain())
                habitDao.upsert(entity.copy(meta = entity.meta.copy(pendingSync = false))) // Changed dao to habitDao
            }

            /* 2) PULL remotos y resolver LWW */
            val remotes = remote.fetchUserHabits(uid)
            remotes.forEach { remoteHabit ->
                val local = habitDao.findById(remoteHabit.id.raw)          // suspend OK aquí, changed dao to habitDao
                val winner = resolveLww(local, remoteHabit)
                habitDao.upsert(winner.toEntity()) // Changed dao to habitDao
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