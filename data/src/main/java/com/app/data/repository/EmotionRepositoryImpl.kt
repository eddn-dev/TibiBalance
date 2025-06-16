/**
 * @file    EmotionRepositoryImpl.kt
 * @ingroup data_repository_impl
 */
package com.app.data.repository

import com.app.data.local.dao.EmotionEntryDao
import com.app.data.local.entities.EmotionEntryEntity
import com.app.data.mappers.EmotionMappers.toDomain
import com.app.data.mappers.EmotionMappers.toEntity
import com.app.data.remote.firebase.EmotionFirestoreService
import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.app.domain.repository.AuthRepository
import com.app.domain.repository.EmotionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
@Singleton
class EmotionRepositoryImpl @Inject constructor(
    private val dao      : EmotionEntryDao,
    private val remote   : EmotionFirestoreService,
    private val authRepo : AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : EmotionRepository {

    /* ─────────── observación ─────────────────────────────── */

    override fun observeAll(): Flow<List<EmotionEntry>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }

    /* ─────────── creación/actualización ───────────────────── */

    /**
     * Guarda/actualiza la entrada **solo en Room** y marca
     * `pendingSync = true`.  El SyncWorker o `syncNow()` se
     * encargarán de subirla a Firestore más tarde.
     */
    override suspend fun upsert(entry: EmotionEntry) = withContext(io) {
        val now = Clock.System.now()
        val localCopy = entry.copy(
            meta = entry.meta.copy(
                createdAt   = entry.meta.createdAt.takeUnless {
                    it == SyncMeta().createdAt
                } ?: now,
                updatedAt   = now,
                pendingSync = true               // ← clave
            )
        )
        dao.upsert(localCopy.toEntity())
        // nada más: 100 % offline-safe ✔️
    }

    /* ─────────── sincronización integral ──────────────────── */

    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = authRepo.authState().first()
            ?: return@withContext Result.failure(IllegalStateException("No user"))

        runCatching {
            /* 1️⃣ PUSH locales pendientes */
            dao.pendingToSync().forEach { local ->
                remote.push(uid, local.toDomain())
                dao.upsert(
                    local.copy(
                        meta = local.meta.copy(pendingSync = false)
                    )
                )
            }

            /* 2️⃣ PULL remotos + LWW */
            remote.fetchAll(uid).forEach { remoteEntry ->
                val local = dao.findByDate(remoteEntry.date)
                val winner = resolveLww(local, remoteEntry)
                dao.upsert(winner.toEntity())
            }
        }
    }

    /* ─────────── consulta puntual ─────────────────────────── */

    override suspend fun hasEntryFor(date: LocalDate): Boolean =
        withContext(io) { dao.findByDate(date) != null }

    /* ─────────── helpers ─────────────────────────────────── */

    private fun resolveLww(
        local: EmotionEntryEntity?,
        remote: EmotionEntry
    ): EmotionEntry =
        local?.toDomain()?.let {
            if (it.meta.updatedAt >= remote.meta.updatedAt) it else remote
        } ?: remote
}

