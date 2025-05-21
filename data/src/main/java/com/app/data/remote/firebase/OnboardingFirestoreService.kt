package com.app.data.remote.firebase

/**
 * @file    OnboardingFirestoreService.kt
 * @ingroup data_remote
 * @brief   Thin wrapper alrededor de la colección `users/{uid}/onboarding`.
 */

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class OnboardingFirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {
    private fun doc(uid: String) =
        db.collection("users").document(uid).collection("meta").document("onboarding")

    suspend fun fetch(uid: String) =
        doc(uid).get().await()

    suspend fun push(uid: String, payload: Map<String, Any?>) =
        doc(uid).set(payload, SetOptions.merge()).await()
}
