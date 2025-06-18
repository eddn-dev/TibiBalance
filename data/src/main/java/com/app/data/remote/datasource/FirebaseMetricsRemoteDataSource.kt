/* :data/remote/datasource/FirebaseMetricsRemoteDataSource.kt */
package com.app.data.remote.datasource

import com.app.data.remote.model.DailyMetricsDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMetricsRemoteDataSource @Inject constructor(
    private val db  : FirebaseFirestore,
    private val json: Json
) : MetricsRemoteDataSource {

    /* users/{uid}/metrics/daily/{yyyy-MM-dd} */
    private fun dailyCol(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("metrics")
            .document("daily")
            .collection("items")            // extra level keeps docs flat (≤1 MB each)  :contentReference[oaicite:1]{index=1}

    private fun Instant?.epochMs() = this?.toEpochMilliseconds() ?: 0L

    override suspend fun pushDailyMetric(uid: String, metric: DailyMetricsDto) {
        val docId = metric.date                                 // “2025-06-18”
        val payload = mapOf(
            "data"      to json.encodeToString(metric),         // store as JSON string  :contentReference[oaicite:2]{index=2}
            "updatedAt" to Instant.parse("${metric.date}T00:00:00Z").epochMs()
        )
        dailyCol(uid)
            .document(docId)
            .set(payload, SetOptions.merge())                   // merge = idempotent  :contentReference[oaicite:3]{index=3}
            .await()
    }
}
