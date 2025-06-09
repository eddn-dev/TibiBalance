/**
 * @file    FirebaseAchievementRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Implementación Firebase/Firestore del contrato [AchievementRemoteDataSource].
 */
package com.app.data.remote.datasource

import android.util.Log
import com.app.data.remote.model.*
import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAchievementRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore
) : AchievementRemoteDataSource {

    /* ─── helpers de colección ───────────────────────────────────────── */

    private fun coll(uid: String) =
        db.collection("users").document(uid).collection("achievements")

    /* ─── API pública ─────────────────────────────────────────────────── */

    override suspend fun fetchAll(uid: String): List<Achievement> = runCatching {
        val snap = coll(uid).get().await()
        snap.documents.mapNotNull { doc ->
            doc.data?.toAchievementDto(doc.id)?.toDomain()
        }
    }.getOrElse {
        Log.e("Achievements", "❌ fetchAll", it)
        emptyList()
    }

    override suspend fun push(uid: String, achievement: Achievement) {
        val dto = achievement.toDto()
        runCatching {
            coll(uid).document(dto.id)
                .set(dto.toMap(), SetOptions.merge())
                .await()
        }.onFailure {
            Log.e("Achievements", "❌ push ${dto.id}", it)
        }
    }

    override suspend fun delete(uid: String, id: AchievementId) {
        runCatching { coll(uid).document(id.raw).delete().await() }
            .onFailure { Log.e("Achievements", "❌ delete ${id.raw}", it) }
    }
}
