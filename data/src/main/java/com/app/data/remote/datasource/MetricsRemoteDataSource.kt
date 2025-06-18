/* :data/remote/datasource/MetricsRemoteDataSource.kt */
package com.app.data.remote.datasource

import com.app.data.remote.model.DailyMetricsDto

/**
 * Remote writes for the daily step-/kcal aggregates.
 *
 * Path: users/{uid}/metrics/daily/{yyyy-MM-dd}
 * Field layout:
 *   └─ data      – full JSON of the DTO
 *   └─ updatedAt – epoch-millis (LWW conflict-free)
 */
interface MetricsRemoteDataSource {
    suspend fun pushDailyMetric(uid: String, metric: DailyMetricsDto)
}
