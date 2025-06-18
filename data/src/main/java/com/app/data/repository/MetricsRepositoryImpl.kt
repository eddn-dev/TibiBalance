/* :data/repository/MetricsRepositoryImpl.kt */
package com.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import com.app.data.local.dao.DailyMetricsDao
import com.app.data.local.entities.DailyMetricsEntity
import com.app.data.mappers.*
import com.app.data.remote.datasource.MetricsRemoteDataSource
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.DailyMetrics
import com.app.domain.entities.DashboardSnapshot
import com.app.domain.repository.MetricsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/* :data/repository/MetricsRepositoryImpl.kt */
@Singleton
class MetricsRepositoryImpl @Inject constructor(
    private val hcClient    : HealthConnectClient?,   // ← nullable
    private val dailyDao    : DailyMetricsDao,
    private val remote      : MetricsRemoteDataSource,
    private val uidProvider : AuthUidProvider,
    @IoDispatcher private val io: CoroutineDispatcher = Dispatchers.IO
) : MetricsRepository {

    /* ───── Dashboard en vivo ───────────────────────────────────────── */

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun observeDashboard(): Flow<DashboardSnapshot?> =
        if (hcClient == null) {
            /* Health Connect ausente ⇒ emitimos null una sola vez          */
            flowOf(null)
        } else channelFlow {
            /* -------- helpers internos (hcClient NO es null aquí) ------- */

            suspend fun todayAgg(): Pair<Int, Int> = withContext(io) {
                val tz       = TimeZone.currentSystemDefault()
                val midnight = Clock.System.todayIn(tz).atStartOfDayIn(tz).toJavaInstant()
                val now      = Clock.System.now().toJavaInstant()

                val agg = hcClient.aggregate(
                    AggregateRequest(
                        metrics = setOf(
                            StepsRecord.COUNT_TOTAL,
                            ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                        ),
                        timeRangeFilter = TimeRangeFilter.between(midnight, now)
                    )
                )

                val steps  = (agg[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
                val kcal   = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                    ?.inKilocalories?.roundToInt() ?: 0
                steps to kcal
            }

            suspend fun latestHr(): Pair<Int?, Long> = withContext(io) {
                val now       = Clock.System.now()
                val startJava = now.minus(30.seconds).toJavaInstant()

                val rec = hcClient.readRecords(
                    ReadRecordsRequest(
                        recordType      = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.after(startJava),
                        pageSize        = 1
                    )
                ).records.firstOrNull()

                val sample = rec?.samples?.lastOrNull()
                val bpm    = sample?.beatsPerMinute?.toInt()
                val ageMs  = sample?.time?.toKotlinInstant()?.let { now - it }?.inWholeMilliseconds
                    ?: Long.MAX_VALUE
                bpm to ageMs
            }

            /* ① Snapshot inicial */
            launch {
                val (s, k) = todayAgg()
                val (hr, age) = latestHr()
                send(DashboardSnapshot(s, k, hr, age))
            }

            /* ② Snapshot cada 30 s */
            val ticker = ticker(30.seconds.inWholeMilliseconds)
            for (tick in ticker) {
                val (s, k) = todayAgg()
                val (hr, age) = latestHr()
                send(DashboardSnapshot(s, k, hr, age))
            }
            awaitClose { ticker.cancel() }
        }.flowOn(io)

    /* ───── Históricos ─────────────────────────────────────────────── */

    override fun observeDailyMetrics(
        startDate: LocalDate,
        endDate  : LocalDate
    ): Flow<List<DailyMetrics>> =
        dailyDao.observeRange(startDate, endDate)
            .map { it.map(DailyMetricsEntity::toDomain) }
            .flowOn(io)

    /* ───── Sincronización “ayer” ──────────────────────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncDailyMetrics(): Result<Unit> = runCatching {
        if (hcClient == null) error("Health Connect no disponible")

        withContext(io) {
            val uid  = uidProvider()
            val tz   = TimeZone.currentSystemDefault()
            val yest = Clock.System.todayIn(tz).minus(DatePeriod(days = 1))

            val start = yest.atStartOfDayIn(tz).toJavaInstant()
            val end   = start.plusSeconds(86_400)

            val agg = hcClient.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )

            val steps = (agg[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
            val kcal  = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
                ?.inKilocalories?.roundToInt() ?: 0

            val daily = DailyMetrics(yest, steps, kcal)

            dailyDao.upsert(daily.toEntity())
            remote.pushDailyMetric(uid, daily.toDto())
            dailyDao.deleteOlderThan(yest.minus(DatePeriod(days = 30)))
        }
    }
}
