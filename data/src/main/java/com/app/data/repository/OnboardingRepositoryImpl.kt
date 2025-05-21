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
                    "tutorialCompleted" to status.tutorialCompleted,
                    "legalAccepted"     to status.legalAccepted,
                    "permissionsAsked"  to status.permissionsAsked,
                    "completedAt"       to status.completedAt?.toString(),
                    "updatedAt"         to status.meta.updatedAt.toString()
                )
            )
        }
}
