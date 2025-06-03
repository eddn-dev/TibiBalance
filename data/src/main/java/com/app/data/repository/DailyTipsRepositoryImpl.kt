/* :data/repository/DailyTipsRepositoryImpl.kt */
package com.app.data.repository

import android.util.Log
import com.app.data.local.dao.DailyTipDao
import com.app.data.mappers.toDomain
import com.app.data.mappers.toEntity
import com.app.data.remote.datasource.DailyTipsRemoteDataSource
import com.app.domain.entities.DailyTip
import com.app.domain.repository.DailyTipsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyTipsRepositoryImpl @Inject constructor(
    private val dao: DailyTipDao,
    private val remote: DailyTipsRemoteDataSource,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : DailyTipsRepository {

    /* :data/repository/DailyTipsRepositoryImpl.kt */
    override fun todayTip(): Flow<DailyTip?> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // ¿Ya existe tip fijado?
        var entity = dao.currentTip(today)

        // Si no, busca uno elegible y márcalo
        if (entity == null) {
            entity = dao.pickRandomEligible(today)
            entity?.let { dao.markShown(it.id, today) }
            // Si tampoco hay ⇒ refrescamos remoto
            if (entity == null) {
                refresh()
                entity = dao.pickRandomEligible(today)
                entity?.let { dao.markShown(it.id, today) }
            }
        }

        emit(entity?.toDomain())          // puede ser null si BD sigue vacía
    }.flowOn(dispatcher)


    override suspend fun refresh(): Result<Unit> = withContext(dispatcher) {
        runCatching {
            val tips = remote.fetchAll()
            // Mostrar el contenido de cada tip en Log
            tips.forEach {
                Log.d("DailyTips", "Tip: $it")
            }

            dao.upsertAll(tips.map { it.toEntity() })
        }
            .onFailure {
                Log.e("DailyTips", "❌ error upserting tips", it)
            }
            .onSuccess {
                Log.d("DailyTips", "🔥 upserted tips")
            }
    }

    override suspend fun markAsShown(id: Int) = withContext(dispatcher) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        dao.markShown(id, today)
    }
}
