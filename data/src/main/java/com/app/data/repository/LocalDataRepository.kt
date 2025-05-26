/* :data/repository/LocalDataRepositoryImpl.kt */
package com.app.data.repository

import com.app.data.local.db.AppDb
import com.app.domain.repository.LocalDataRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class LocalDataRepositoryImpl @Inject constructor(
    private val db: AppDb,               // Room
    @IoDispatcher private val io: CoroutineDispatcher
) : LocalDataRepository {

    override suspend fun clearAll() = withContext(io) {
        /* orden de borrado si hay FK */
        db.emotionDao().clear()
        db.habitDao().clear()
        db.userDao().clear()
    }
}
