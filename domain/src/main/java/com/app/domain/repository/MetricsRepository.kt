/* :domain/repository/MetricsRepository.kt */
package com.app.domain.repository

import com.app.domain.entities.DailyMetrics          // steps + kcal (Room/Firestore)
import com.app.domain.entities.DashboardSnapshot        // live UI card
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Single-source-of-truth for steps/kcal aggregates + live heart-rate.
 *
 * Implemented in :data (HealthConnectRepoImpl).
 */
interface MetricsRepository {

    /* ─── Reactive streams ───────────────────────────────────────── */

    /** Emits whenever steps/kcal or last heart-rate change (Home screen). */
    fun observeDashboard(): Flow<DashboardSnapshot>

    /**
     * Emits cached daily aggregates for an inclusive date range
     * (`start ≤ date ≤ end`).  Use it for “last 7 days / last 30 days”
     * charts and streak widgets.
     */
    fun observeDailyMetrics(
        startDate: LocalDate,
        endDate  : LocalDate
    ): Flow<List<DailyMetrics>>

    /* ─── Commands / side-effects ───────────────────────────────── */

    /**
     * 1️⃣ Aggregates **yesterday’s** steps & kcal via Health Connect,
     *    upserts into Room and pushes to Firestore (LWW).
     * 2️⃣ Cleans local rows older than 30 days (retention-policy).
     */
    suspend fun syncDailyMetrics(): Result<Unit>
}
