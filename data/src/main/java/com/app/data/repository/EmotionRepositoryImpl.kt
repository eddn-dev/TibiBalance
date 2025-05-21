/**
 * @file    EmotionRepositoryImpl.kt
 * @ingroup data_repository_impl
 * @brief   Implementación offline-first para registros emocionales.
 */
package com.app.data.repository

import com.app.data.local.dao.EmotionEntryDao
import com.app.data.mappers.EmotionMappers.toDomain
import com.app.data.mappers.EmotionMappers.toEntity
import com.app.data.remote.firebase.EmotionFirestoreService
import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.app.domain.repository.AuthRepository            // 👈 NEW
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
    private val dao       : EmotionEntryDao,
    private val remote    : EmotionFirestoreService,
    private val authRepo  : AuthRepository,          // 👈 NEW (para obtener el UID)
    @IoDispatcher private val io: CoroutineDispatcher
) : EmotionRepository {

    override fun observeAll(): Flow<List<EmotionEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(entry: EmotionEntry) = withContext(io) {
        /* 1 ▸ actualiza metadatos LWW */
        val now  = Clock.System.now()
        val newMeta = entry.meta.copy(
            createdAt   = if (entry.meta.createdAt == SyncMeta().createdAt) now else entry.meta.createdAt,
            updatedAt   = now,
            pendingSync = true               // siempre true al principio
        )
        val toSave = entry.copy(meta = newMeta)

        /* 2 ▸ guarda localmente */
        dao.upsert(toSave.toEntity())

        /* 3 ▸ intenta subir a Firestore */
        val uid = authRepo.authState().first()     // UID o null
        if (uid != null) {
            try {
                remote.push(
                    uid   = uid,
                    entry = mapOf(
                        "date"       to toSave.date.toString(),
                        "emojiId"    to toSave.emojiId,
                        "createdAt"  to toSave.meta.createdAt.toString(),
                        "updatedAt"  to toSave.meta.updatedAt.toString(),
                        "deletedAt"  to toSave.meta.deletedAt?.toString()
                    )
                )
                /* 4 ▸ éxito: limpia pendingSync */
                dao.upsert(toSave.copy(meta = toSave.meta.copy(pendingSync = false)).toEntity())
            } catch (_: Exception) {
                // sin conexión: se queda pendingSync=true; el SyncWorker lo sincronizará luego
            }
        }
    }
}
