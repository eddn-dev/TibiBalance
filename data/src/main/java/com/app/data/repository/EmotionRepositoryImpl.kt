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

@Singleton
class EmotionRepositoryImpl @Inject constructor(
    private val dao      : EmotionEntryDao,
    private val remote   : EmotionFirestoreService,
    private val authRepo : AuthRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : EmotionRepository {

    /* ─────────── observación ─────────────────────────────── */

    override fun observeAll(): Flow<List<EmotionEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /* ─────────── creación/actualización ───────────────────── */

    override suspend fun upsert(entry: EmotionEntry) = withContext(io) {
        /* 1 ▸ actualiza metadatos LWW */
        val now     = Clock.System.now()
        val newMeta = entry.meta.copy(
            createdAt   = if (entry.meta.createdAt == SyncMeta().createdAt) now else entry.meta.createdAt,
            updatedAt   = now,
            pendingSync = true
        )
        val localCopy = entry.copy(meta = newMeta)

        /* 2 ▸ guarda localmente */
        dao.upsert(localCopy.toEntity())

        /* 3 ▸ intenta subir a Firestore (tipado) */
        val uid = authRepo.authState().first() ?: return@withContext
        try {
            remote.push(uid, localCopy)           // ← sobrecarga tip-safe
            dao.upsert(
                localCopy.copy(meta = localCopy.meta.copy(pendingSync = false)).toEntity()
            )
        } catch (_: Exception) {
            /* offline: pendingSync se mantiene en true, lo tomará el worker */
        }
    }

    /* ─────────── sincronización integral ──────────────────── */

    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = authRepo.authState().first()
            ?: return@withContext Result.failure(IllegalStateException("No user"))

        runCatching {
            /* 1️⃣ PUSH locales pendientes */
            dao.pendingToSync().forEach { local ->
                remote.push(uid, local.toDomain())
                dao.upsert(local.copy(meta = local.meta.copy(pendingSync = false)))
            }

            /* 2️⃣ PULL remotos y LWW */
            remote.fetchAll(uid).forEach { remoteEntry ->
                val local = dao.findByDate(remoteEntry.date)
                val winner = resolveLww(local, remoteEntry)
                dao.upsert(winner.toEntity())
            }
        }
    }

    /* ─────────── helpers ─────────────────────────────────── */

    private fun resolveLww(
        local: EmotionEntryEntity?,
        remote: EmotionEntry
    ): EmotionEntry =
        local?.toDomain()?.let {
            if (it.meta.updatedAt >= remote.meta.updatedAt) it else remote
        } ?: remote
}
