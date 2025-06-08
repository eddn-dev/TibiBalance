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

import android.util.Log

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


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
            .map { list -> list
                .filter { it.meta.deletedAt == null }   // por si el DAO fallara
                .map   { it.toDomain() }
            }
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
        dao.observeById(id.raw)
            .map { it?.toDomain() }
            .distinctUntilChanged()

    /* ─────────────────────── operaciones CRUD ────────────────── */

    /* ──────────── CREATE (Room-only) ──────────── */
    override suspend fun create(habit: Habit) = withContext(io) {
        dao.upsert(
            habit.copy(
                meta = habit.meta.copy(
                    createdAt   = Clock.System.now(),
                    pendingSync = true        // ← lo enviará el SyncWorker
                )
            ).toEntity()
        )
        Unit
    }

    /* ──────────── UPDATE (Room-only) ──────────── */
    override suspend fun update(habit: Habit) = withContext(io) {
        require(!habit.isBuiltIn) { "Plantillas sugeridas no son editables" }
        dao.upsert(
            habit.copy(
                meta = habit.meta.copy(
                    updatedAt   = Clock.System.now(),
                    pendingSync = true
                )
            ).toEntity()
        )
        Unit
    }

    /* ──────────── DELETE (Room-only Tombstone) ──────────── */
    override suspend fun delete(id: HabitId) = withContext(io) {
        dao.findById(id.raw)?.let { row ->
            dao.upsert(
                row.copy(
                    meta = row.meta.copy(
                        deletedAt   = Clock.System.now(),
                        updatedAt   = Clock.System.now(),
                        pendingSync = true
                    )
                )
            )
        }
        Unit
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

    /* ───────────────────────── habito de salud ───────────────────────── */
    override suspend fun evaluateAchievements(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        Log.d("AchievementsCheck", "Evaluando logros para el usuario: $userId")

        val habitos = firestore.collection("users")
            .document(userId)
            .collection("habits")
            .get()
            .await()

        Log.d("AchievementsCheck", "Hábitos obtenidos: ${habitos.size()}")

        val tieneBienestar = habitos.any {
            val categoria = it.getString("category")?.lowercase()
            Log.d("AchievementsCheck", "Revisando hábito con categoría: $categoria")
            categoria == "bienestar"
        }

        Log.d("AchievementsCheck", "¿Tiene hábito de bienestar?: $tieneBienestar")

        if (tieneBienestar) {
            val logroRef = firestore.collection("users")
                .document(userId)
                .collection("achievements")
                .document("tibio_bienestar")

            Log.d("AchievementsCheck", "Actualizando logro 'tibio_bienestar'...")

            logroRef.set(
                mapOf(
                    "name" to "Tibio del bienestar",
                    "description" to "Agrega un hábito de bienestar",
                    "progress" to 100,
                    "unlocked" to true,
                    "unlockDate" to Timestamp.now()
                ),
                SetOptions.merge()
            ).addOnSuccessListener {
                Log.d("AchievementsCheck", "Logro 'tibio_bienestar' actualizado correctamente.")
            }.addOnFailureListener {
                Log.e("AchievementsCheck", "Error al actualizar el logro: ${it.message}", it)
            }
        } else {
            Log.d("AchievementsCheck", "No se encontró hábito de bienestar, no se actualiza el logro.")
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