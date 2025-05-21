/**
 * @file    EmotionFirestoreService.kt
 */
package com.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EmotionFirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun doc(uid: String, date: String) =
        db.collection("users").document(uid)
            .collection("emotions").document(date)

    suspend fun fetchAll(uid: String) =
        db.collection("users").document(uid)
            .collection("emotions").get().await()

    suspend fun push(uid: String, entry: Map<String, Any?>) =
        doc(uid, entry["date"] as String).set(entry).await()
}
