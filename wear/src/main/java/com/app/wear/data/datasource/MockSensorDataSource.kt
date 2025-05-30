package com.app.wear.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class MockSensorDataSource @Inject constructor() : ISensorDataSource {
    override fun getHeartRateUpdates(): Flow<Int> = flow { emit(Random.nextInt(60, 100)) }
    override suspend fun fetchCurrentStepCount(): Int = Random.nextInt(0, 5000)
}
