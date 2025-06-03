/* :data/remote/datasource/FirebaseDailyTipsRemoteDataSource.kt */
package com.app.data.remote.datasource

import android.util.Log
import com.app.data.remote.model.DailyTipDto
import com.app.domain.entities.DailyTip
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDailyTipsRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore,
) : DailyTipsRemoteDataSource  {

    private val tipsColl get() = db.collection("dailyActivityTips")

    /** Descarga todos los tips activos (o lista vacía si falla). */
    override suspend fun fetchAll(): List<DailyTip> = try {
        val list = tipsColl.get().await()
            .toObjects(DailyTipDto::class.java)
            .map { it.toDomain() }
        Log.d("DailyTips", "🔥 descargados ${list.size} tips")
        list
    } catch (e: Exception) {
        Log.e("DailyTips", "❌ error al descargar", e)
        emptyList()
    }
}
