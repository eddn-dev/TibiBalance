package com.app.data.metrics.repository

import com.app.data.metrics.local.UserMetricsDao
import com.app.data.metrics.mapper.toDomain
import com.app.data.metrics.mapper.toDto
import com.app.data.metrics.mapper.toEntity
import com.app.data.metrics.remote.FirebaseUserMetricsRemoteDataSource
import com.app.domain.metrics.entity.UserMetrics
import com.app.domain.metrics.repository.UserMetricsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementación de [UserMetricsRepository] que orquesta Room + Firestore.
 */
class UserMetricsRepositoryImpl @Inject constructor(
    private val dao: UserMetricsDao,
    private val remote: FirebaseUserMetricsRemoteDataSource,
    @Named("IoDispatcher") private val dispatcher: CoroutineDispatcher
) : UserMetricsRepository {

    override suspend fun sync(metrics: List<UserMetrics>) = withContext(dispatcher) {
        // Inserta localmente
        dao.insertAll(metrics.map { it.toEntity() })
        // Sube remoto
        remote.pushAll(metrics.map { it.toDto() })
    }

    override suspend fun update(metric: UserMetrics) = withContext(dispatcher) {
        dao.update(metric.toEntity())
        remote.update(metric.toDto())
    }

    override suspend fun deleteByDate(date: LocalDate) = withContext(dispatcher) {
        // Obtener userId desde Auth (ajusta si usas otro módulo)
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().uid
            ?: throw IllegalStateException("No authenticated user")
        dao.deleteByDate(date)
        remote.delete(userId, date.toString())
    }

    override fun observe(from: LocalDate, to: LocalDate): Flow<List<UserMetrics>> =
        dao.observe(from, to).map { list ->
            list.map { it.toDomain() }
        }
}
