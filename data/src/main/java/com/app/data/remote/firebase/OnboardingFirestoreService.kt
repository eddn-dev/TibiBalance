/**
 * @file    OnboardingFirestoreService.kt
 * @ingroup data_remote
 * @brief   Wrapper typed-safe sobre `users/{uid}/meta/onboarding`.
 */
package com.app.data.remote.firebase

import com.app.domain.common.SyncMeta
import com.app.domain.entities.OnboardingStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant

class OnboardingFirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {

    /* ───── helpers ───────────────────────────── */
    private fun doc(uid: String) =
        db.collection("users").document(uid)
            .collection("meta").document("onboarding")

    /* ───── lecturas ──────────────────────────── */

    /** Devuelve `null` si el documento aún no existe en Firestore. */
    suspend fun fetch(uid: String): OnboardingStatus? {
        val snap = doc(uid).get().await()
        if (!snap.exists()) return null

        fun Any?.toBoolOrNull(): Boolean? = when (this) {
            is Boolean -> this
            is String -> this == "true"
            else -> null
        }

        return OnboardingStatus(
            hasCompletedTutorial = snap.get("hasCompletedTutorial")?.toBoolOrNull() ?: false,
            tutorialCompleted = snap.get("tutorialCompleted")?.toBoolOrNull() ?: false,
            legalAccepted = snap.get("legalAccepted")?.toBoolOrNull() ?: false,
            permissionsAsked = snap.get("permissionsAsked")?.toBoolOrNull() ?: false,
            completedAt = snap.getString("completedAt")?.let(Instant::parse),
            meta = SyncMeta(
                updatedAt = snap.getString("updatedAt")?.let(Instant::parse) ?: Instant.DISTANT_PAST,
                pendingSync = false
            ),
            hasSeenTutorial_HomeScreenMain = snap.get("hasSeenTutorial_HomeScreenMain")?.toBoolOrNull() ?: false,
            hasSeenTutorial_HomeScreenStats = snap.get("hasSeenTutorial_HomeScreenStats")?.toBoolOrNull() ?: false,
            hasSeenTutorial_HabitsScreen = snap.get("hasSeenTutorial_HabitsScreen")?.toBoolOrNull() ?: false,
            hasSeenTutorial_EmotionsScreen = snap.get("hasSeenTutorial_EmotionsScreen")?.toBoolOrNull() ?: false,
            hasSeenTutorial_SettingsScreen = snap.get("hasSeenTutorial_SettingsScreen")?.toBoolOrNull() ?: false
        )
    }



    /* ───── escrituras ────────────────────────── */

    /* 🔄  Sobrecarga legacy — mantiene compatibilidad con save() existente */
    suspend fun push(uid: String, payload: Map<String, Any?>) =
        doc(uid).set(payload, SetOptions.merge()).await()
}
