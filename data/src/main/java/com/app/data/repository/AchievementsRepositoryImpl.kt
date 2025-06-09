// :data/repository/AchievementsRepositoryImpl.kt
package com.app.data.repository

import android.util.Log
import com.app.data.local.dao.AchievementDao
import com.app.data.mappers.AchievementMappers.toDomain
import com.app.data.mappers.AchievementMappers.toEntity
import com.app.data.remote.datasource.AchievementRemoteDataSource
import com.app.domain.achievements.DefaultAchievements    // (ver §3)
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId
import com.app.domain.repository.AchievementsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

@Singleton
class AchievementsRepositoryImpl @Inject constructor(
    private val dao       : AchievementDao,
    private val remote    : AchievementRemoteDataSource,
    private val uidProvider : AuthUidProvider,
    @IoDispatcher private val io: CoroutineDispatcher
) : AchievementsRepository {

    /* --------------- lectura --------------- */
    override fun observeAll(): Flow<List<Achievement>> =
        dao.observeAll()
            .map { it.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun find(id: AchievementId): Achievement? =
        dao.findById(id.raw)?.toDomain()

    /* --------------- escritura -------------- */
    override suspend fun upsert(achievement: Achievement) = withContext(io) {
        dao.upsert(achievement.toEntity())
    }

    override suspend fun updateProgress(id: AchievementId, progress: Int) = withContext(io) {
        dao.findById(id.raw)?.let { row ->
            dao.upsert(
                row.copy(
                    progress = progress,
                    meta = row.meta.copy(
                        updatedAt   = Clock.System.now(),
                        pendingSync = true
                    )
                )
            )
        }
        ?: throw IllegalStateException("Achievement not found")
    }

    /* --------------- seed inicial ----------- */
    override suspend fun initializeDefaults() = withContext(io) {
        val existing = dao.allIds().toSet()
        val now      = Clock.System.now()

        val toInsert = DefaultAchievements.list
            .filter { it.id.raw !in existing }
            .map { ach ->
                ach.copy(
                    meta = ach.meta.copy(
                        createdAt   = now,
                        updatedAt   = now,
                        pendingSync = true   // ← para que el Worker los suba
                    )
                ).toEntity()
            }

        if (toInsert.isNotEmpty()) {
            dao.upsert(toInsert)
            Log.d("AchievementsRepo", "Seeded ${toInsert.size} achievements")
        }
    }
    /* --------------- sync ------------------- */
    override suspend fun syncNow(): Result<Unit> = withContext(io) {
        val uid = uidProvider()
        Log.d("AchievementsRepositoryImpl", "Syncing achievements for user $uid")

        runCatching {
            /* 1 ▸ PUSH pendientes */
            dao.pendingToSync().forEach { local ->
                remote.push(uid, local.toDomain())
                dao.upsert(local.copy(meta = local.meta.copy(pendingSync = false)))
                Log.d("AchievementsRepositoryImpl", "Synced achievement ${local.id}")
            }

            /* 2 ▸ PULL y LWW */
            remote.fetchAll(uid).forEach { remoteAch ->
                val local  = dao.findById(remoteAch.id.raw)
                val winner = resolveLww(local?.toDomain(), remoteAch)
                dao.upsert(winner.toEntity())
            }
        }
    }

    private fun resolveLww(local: Achievement?, remote: Achievement): Achievement =
        when {
            local == null -> remote
            local.meta.updatedAt >= remote.meta.updatedAt -> local
            else -> remote
        }
}
