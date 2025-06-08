/**
 * @file    OnboardingRepositoryImpl.kt
 * @ingroup data_repository_impl
 * @brief   Implementación offline-first con Room + Firestore.
 */
package com.app.data.repository

import com.app.data.local.dao.OnboardingStatusDao
import com.app.data.mappers.OnboardingMappers.toDomain
import com.app.data.mappers.OnboardingMappers.toEntity
import com.app.data.remote.firebase.OnboardingFirestoreService
import com.app.domain.entities.OnboardingStatus
import com.app.domain.repository.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val dao: OnboardingStatusDao,
    private val remote: OnboardingFirestoreService,
    @IoDispatcher private val io: CoroutineDispatcher   // <- viene de tu DispatcherModule
) : OnboardingRepository {

    /* ───────── observación ────────────────────────── */

    override fun observe(uid: String): Flow<OnboardingStatus> =
        dao.observe(uid)
            .map { entity ->
                // Si no existe en Room aún, devolvemos el default
                entity?.toDomain() ?: OnboardingStatus()
            }

    /* ───────── escritura (y sync) ─────────────────── */

    override suspend fun save(uid: String, status: OnboardingStatus): Unit =
        withContext(io) {
            // 1. Actualiza local (Room)
            dao.upsert(status.toEntity(uid))

            // 2. Sube a Firestore (Last-Write-Wins simplón)
            remote.push(
                uid,
                mapOf(
                    "hasCompletedTutorial" to status.hasCompletedTutorial,
                    "tutorialCompleted" to status.tutorialCompleted,
                    "legalAccepted"     to status.legalAccepted,
                    "permissionsAsked"  to status.permissionsAsked,
                    "completedAt"       to status.completedAt?.toString(),
                    "updatedAt"         to status.meta.updatedAt.toString()
                )
            )
        }

    override suspend fun saveTutorialStatus(uid: String, completed: Boolean) {
        val current = dao.find(uid)?.toDomain() ?: OnboardingStatus()
        val updated = current.copy(
            hasCompletedTutorial = completed,
            meta = current.meta.copy(pendingSync = true)
        )
        save(uid, updated)
    }

    override suspend fun syncNow(uid: String): Result<Unit> = withContext(io) {
        return@withContext runCatching {
            /* 1️⃣  PULL remoto primero (un solo doc) */
            val remoteStatus = remote.fetch(uid)          // -- puede devolver null
            val localStatus  = dao.find(uid)              // -- puede devolver null

            val winner = when {
                remoteStatus == null -> localStatus?.toDomain() ?: OnboardingStatus()
                localStatus == null  -> remoteStatus
                localStatus.meta.updatedAt >= remoteStatus.meta.updatedAt -> localStatus.toDomain()
                else -> remoteStatus
            }

            dao.upsert(winner.toEntity(uid))

            /* 3️⃣  PUSH si el local era el más nuevo o si quedaba pendingSync */
            if (winner.meta.pendingSync || winner === localStatus?.toDomain()) {
                remote.push(
                    uid,
                    mapOf(
                        "hasCompletedTutorial" to winner.hasCompletedTutorial,
                        "tutorialCompleted" to winner.tutorialCompleted,
                        "legalAccepted"     to winner.legalAccepted,
                        "permissionsAsked"  to winner.permissionsAsked,
                        "completedAt"       to winner.completedAt?.toString(),
                        "updatedAt"         to winner.meta.updatedAt.toString()
                    )
                )
                dao.upsert(
                    winner.copy(meta = winner.meta.copy(pendingSync = false)).toEntity(uid)
                )
            }
        }
    }
}
