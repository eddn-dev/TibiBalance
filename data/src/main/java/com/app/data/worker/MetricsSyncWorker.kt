// data/src/main/java/com/app/data/worker/MetricsSyncWorker.kt
package com.app.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.data.remote.datasourcemetrics.MetricsRemoteDataSource
import com.app.data.remote.datasourcemetrics.model.FirestoreMetricsDto
import com.app.domain.model.DailyMetrics
import com.app.domain.repository.MetricsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Worker que sube periódicamente todas las métricas pendientes
 * (pendingSync = true) desde Room a Firestore, y luego marca
 * esas fechas como sincronizadas.
 */
@HiltWorker
class MetricsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val metricsRepository: MetricsRepository,
    private val remoteDataSource: MetricsRemoteDataSource
) : CoroutineWorker(context, params) {

    private val TAG = "MetricsSyncWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "▶▶ doWork() iniciando…")
        return try {
            // 1) Recuperar todas las métricas de Room
            val allMetrics: List<DailyMetrics> = metricsRepository
                .streamDailyMetrics()
                .first()
            Log.d(TAG, "  • Total métricas en Room = ${allMetrics.size}")

            // 2) Filtrar las pendientes
            val pendingList = allMetrics.filter { it.pendingSync }
            Log.d(TAG, "  • Métricas pendientes = ${pendingList.size}")
            if (pendingList.isEmpty()) {
                Log.d(TAG, "  • No hay pendientes, SUCCESS")
                return Result.success()
            }

            // 3) Subir cada una
            val successfullySyncedDates = mutableListOf<LocalDate>()
            for (metric in pendingList) {
                // Construye el DTO y haz logging
                val dto = FirestoreMetricsDto(
                    date = metric.date.toString(),
                    steps = metric.steps,
                    avgHeart = metric.avgHeart,
                    calories = metric.calories,
                    source = metric.source,
                    importedAtEpoch = metric.importedAt.toEpochMilliseconds()
                )
                Log.d(TAG, "  • Subiendo métrica para date=${metric.date}, dto=$dto")
                remoteDataSource.uploadMetric(dto)
                successfullySyncedDates.add(metric.date)
            }

            // 4) Marcar en Room
            Log.d(TAG, "  • Marcando como sincronizadas fechas=$successfullySyncedDates")
            metricsRepository.markSynced(successfullySyncedDates)
            Log.d(TAG, "  • doWork(): SUCCESS")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "  ✗ doWork(): excepción =", e)
            Result.retry()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): MetricsSyncWorker
    }
    companion object {
        const val WORK_NAME = "metrics_sync"
    }
}
