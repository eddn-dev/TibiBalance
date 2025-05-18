/**
 * @file    HabitTemplateRepositoryImpl.kt
 * @ingroup data_repository
 */
package com.app.data.repository

import com.app.data.local.dao.HabitTemplateDao
import com.app.data.mappers.TemplateMappers.toDomain
import com.app.data.mappers.TemplateMappers.toEntity
import com.app.data.remote.firebase.HabitTemplateService
import com.app.domain.entities.HabitTemplate
import com.app.domain.repository.HabitTemplateRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class IoDispatcher

@Singleton
class HabitTemplateRepositoryImpl @Inject constructor(
    private val dao : HabitTemplateDao,
    private val svc : HabitTemplateService,
    @IoDispatcher private val io: CoroutineDispatcher
) : HabitTemplateRepository {

    /* ------------------- Lectura reactiva ------------------- */
    override val templates: Flow<List<HabitTemplate>> =
        dao.observeAll()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(io)

    /* ------------------- Descarga única --------------------- */
    override suspend fun refreshOnce() = withContext(io) {
        val remote = svc.fetchOnce()                  // → List<HabitTemplate>
        dao.upsert(remote.map { it.toEntity() })
    }

    /* ------------------- Sincronización en vivo ------------- */
    private val syncScope = CoroutineScope(io + SupervisorJob())

    override fun startSync() {
        // Evita lanzar más de un listener
        if (syncScope.coroutineContext[Job]?.isActive == true) return

        syncScope.launch {
            svc.observe().collect { remoteList ->
                dao.upsert(remoteList.map { it.toEntity() })
            }
        }
    }
}
