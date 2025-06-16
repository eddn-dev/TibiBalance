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
    @IoDispatcher private val io: CoroutineDispatcher
) : OnboardingRepository {

    override fun observe(uid: String): Flow<OnboardingStatus> =
        dao.observe(uid).map { entity ->
            entity?.toDomain() ?: OnboardingStatus()
        }

    override suspend fun save(uid: String, status: OnboardingStatus): Unit =
        withContext(io) {
            dao.upsert(status.toEntity(uid))

            remote.push(
                uid,
                mapOf(
                    "hasCompletedTutorial" to status.hasCompletedTutorial,
                    "tutorialCompleted" to status.tutorialCompleted,
                    "legalAccepted" to status.legalAccepted,
                    "permissionsAsked" to status.permissionsAsked,
                    "completedAt" to status.completedAt?.toString(),
                    "updatedAt" to status.meta.updatedAt.toString(),

                    // 👇 Flags individuales (¡no se deben perder!)
                    "hasSeenTutorial_HomeScreenMain" to status.hasSeenTutorial_HomeScreenMain,
                    "hasSeenTutorial_HomeScreenStats" to status.hasSeenTutorial_HomeScreenStats,
                    "hasSeenTutorial_HabitsScreen" to status.hasSeenTutorial_HabitsScreen,
                    "hasSeenTutorial_EmotionsScreen" to status.hasSeenTutorial_EmotionsScreen,
                    "hasSeenTutorial_SettingsScreen" to status.hasSeenTutorial_SettingsScreen
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
        runCatching {
            val remoteStatus = remote.fetch(uid)
            val localStatus = dao.find(uid)?.toDomain()

            val merged = mergeStatus(localStatus, remoteStatus)

            // 1. Guarda localmente lo más completo
            dao.upsert(merged.toEntity(uid))

            // 2. Sube si estaba pendiente o es más reciente
            if (merged.meta.pendingSync || merged == localStatus) {
                remote.push(
                    uid,
                    mapOf(
                        "hasCompletedTutorial" to merged.hasCompletedTutorial,
                        "tutorialCompleted" to merged.tutorialCompleted,
                        "legalAccepted" to merged.legalAccepted,
                        "permissionsAsked" to merged.permissionsAsked,
                        "completedAt" to merged.completedAt?.toString(),
                        "updatedAt" to merged.meta.updatedAt.toString(),

                        // ¡Todos los flags!
                        "hasSeenTutorial_HomeScreenMain" to merged.hasSeenTutorial_HomeScreenMain,
                        "hasSeenTutorial_HomeScreenStats" to merged.hasSeenTutorial_HomeScreenStats,
                        "hasSeenTutorial_HabitsScreen" to merged.hasSeenTutorial_HabitsScreen,
                        "hasSeenTutorial_EmotionsScreen" to merged.hasSeenTutorial_EmotionsScreen,
                        "hasSeenTutorial_SettingsScreen" to merged.hasSeenTutorial_SettingsScreen
                    )
                )
            }
        }
    }

    private fun mergeStatus(
        local: OnboardingStatus?,
        remote: OnboardingStatus?
    ): OnboardingStatus {
        if (local == null) return remote ?: OnboardingStatus()
        if (remote == null) return local

        return OnboardingStatus(
            hasCompletedTutorial = local.hasCompletedTutorial || remote.hasCompletedTutorial,
            tutorialCompleted = local.tutorialCompleted || remote.tutorialCompleted,
            legalAccepted = local.legalAccepted || remote.legalAccepted,
            permissionsAsked = local.permissionsAsked || remote.permissionsAsked,
            completedAt = local.completedAt ?: remote.completedAt,
            meta = local.meta, // usamos meta local si está pendiente

            hasSeenTutorial_HomeScreenMain = local.hasSeenTutorial_HomeScreenMain || remote.hasSeenTutorial_HomeScreenMain,
            hasSeenTutorial_HomeScreenStats = local.hasSeenTutorial_HomeScreenStats || remote.hasSeenTutorial_HomeScreenStats,
            hasSeenTutorial_HabitsScreen = local.hasSeenTutorial_HabitsScreen || remote.hasSeenTutorial_HabitsScreen,
            hasSeenTutorial_EmotionsScreen = local.hasSeenTutorial_EmotionsScreen || remote.hasSeenTutorial_EmotionsScreen,
            hasSeenTutorial_SettingsScreen = local.hasSeenTutorial_SettingsScreen || remote.hasSeenTutorial_SettingsScreen
        )
    }

    override suspend fun getStatus(uid: String): OnboardingStatus = withContext(io) {
        dao.find(uid)?.toDomain() ?: OnboardingStatus()
    }

    override suspend fun saveStatus(uid: String, status: OnboardingStatus): Unit = withContext(io) {
        save(uid, status)
    }
}
