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

@Singleton
class MetricsRepositoryImpl @Inject constructor(
    private val hcClient    : HealthConnectClient,
    private val dailyDao    : DailyMetricsDao,
    private val remote      : MetricsRemoteDataSource,
    private val uidProvider : AuthUidProvider,
    private val io          : CoroutineDispatcher = Dispatchers.IO
) : MetricsRepository {

    /* ───── Dashboard live ──────────────────────────────────────── */

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun observeDashboard(): Flow<DashboardSnapshot> = channelFlow {
        val tz    = TimeZone.currentSystemDefault()
        val scope = this

        suspend fun todayAgg(): Pair<Int, Int> = withContext(io) {
            val tz       = TimeZone.currentSystemDefault()
            val midnight = Clock.System.todayIn(tz).atStartOfDayIn(tz).toJavaInstant()
            val now      = Clock.System.now().toJavaInstant()

            val agg = hcClient.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        StepsRecord.COUNT_TOTAL,                            // ✔️ pasos
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL    // ✔️ kcal
                    ),
                    timeRangeFilter = TimeRangeFilter.between(midnight, now)
                )
            )                                                   /* :contentReference[oaicite:2]{index=2} */

            /* Steps → Long ▸ Int */
            val steps = (agg[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()

            /* Energy → kilocalories ▸ Int */
            val energy: Energy? = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]
            val kcal   = energy?.inCalories?.roundToInt() ?: 0         /* :contentReference[oaicite:3]{index=3} */

            steps to kcal
        }

        /** Helper: última muestra de FC (≤30 s atrás) */
        /* ───── última muestra de frecuencia cardíaca (< 30 s) ──────────── */
        suspend fun latestHr(
            hcClient: HealthConnectClient,
            io: kotlin.coroutines.CoroutineContext = Dispatchers.IO
        ): Pair<Int?, Long> = withContext(io) {
            val now        = Clock.System.now()
            val startJava  = now.minus(30.seconds).toJavaInstant()          // kotlin → java
            val range      = TimeRangeFilter.after(startJava)               // :contentReference[oaicite:5]{index=5}

            val rec = hcClient.readRecords(
                ReadRecordsRequest(
                    recordType      = HeartRateRecord::class,
                    timeRangeFilter = range,
                    pageSize        = 1
                )
            ).records.firstOrNull()                                         // :contentReference[oaicite:6]{index=6}

            val sample    = rec?.samples?.lastOrNull()
            val bpm: Int? = sample?.beatsPerMinute?.toInt()                 // Long → Int :contentReference[oaicite:7]{index=7}
            val ageMs     = sample?.time
                ?.toKotlinInstant()                                         // java → kotlin
                ?.let { now - it }
                ?.inWholeMilliseconds
                ?: Long.MAX_VALUE

            bpm to ageMs
        }

        /* ① Primer snapshot inmediato */
        launch {
            val (s, k) = todayAgg()
            val hr     = latestHr(
                hcClient,
                io
            )
            send(DashboardSnapshot(s, k, hr.first, hr.second))
        }

        /* ② Snapshot periódico cada 30 s */
        val ticker = ticker(30.seconds.inWholeMilliseconds)
        for (tick in ticker) {
            val (s, k) = todayAgg()
            val hr     = latestHr(
                hcClient,
                io
            )
            send(DashboardSnapshot(s, k, hr.first, hr.second))
        }
        awaitClose { ticker.cancel() }
    }.flowOn(io)

    /* ───── Históricos por rango ────────────────────────────────── */

    override fun observeDailyMetrics(
        startDate: LocalDate,
        endDate  : LocalDate
    ): Flow<List<DailyMetrics>> =
        dailyDao.observeRange(startDate, endDate)
            .map { list -> list.map(DailyMetricsEntity::toDomain) }
            .flowOn(io)

    /* ───── Sync diario (ayer) ──────────────────────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncDailyMetrics(): Result<Unit> = runCatching {
        withContext(io) {
            val uid  = uidProvider()
            val tz   = TimeZone.currentSystemDefault()
            val yest = Clock.System.todayIn(tz).minus(DatePeriod(days = 1))

            /* Aggregate ayer */
            val startKt = yest.atStartOfDayIn(tz)                    // kotlinx Instant
            val endKt   = startKt.plus(1, DateTimeUnit.DAY, tz)

            /* CONVIERTE a java.time.Instant */
            val start = startKt.toJavaInstant()
            val end   = endKt.toJavaInstant()

            val req = AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                ),
                timeRangeFilter = TimeRangeFilter.between(start, end)   // ✅ tipos correctos
            )

            val agg = hcClient.aggregate(req)                                 // :contentReference[oaicite:9]{index=9}
            val steps = (agg[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
            val energy: Energy? = agg[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]

            val kcal = energy?.inKilocalories
                ?.roundToInt() ?: 0

            val daily = DailyMetrics(yest, steps, kcal)

            /* Upsert Room & push Firestore */
            dailyDao.upsert(daily.toEntity())                                 // :contentReference[oaicite:10]{index=10}
            remote.pushDailyMetric(uid, daily.toDto())                        // :contentReference[oaicite:11]{index=11}

            /* Retención: purgar >30 d */
            dailyDao.deleteOlderThan(yest.minus(DatePeriod(days = 30)))
        }
    }
}
