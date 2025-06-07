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

        return OnboardingStatus(
            tutorialCompleted = snap.getBoolean("tutorialCompleted") ?: false,
            legalAccepted     = snap.getBoolean("legalAccepted") ?: false,
            permissionsAsked  = snap.getBoolean("permissionsAsked") ?: false,
            hasCompletedTutorial = snap.getBoolean("hasCompletedTutorial") ?: false,
            completedAt       = snap.getString("completedAt")?.let(Instant::parse),
            meta = SyncMeta(
                updatedAt   = snap.getString("updatedAt")?.let(Instant::parse)
                    ?: Instant.DISTANT_PAST,
                pendingSync = false     // Firestore ≡ ya sincronizado
            )
        )
    }

    /* ───── escrituras ────────────────────────── */

    /* 🔄  Sobrecarga legacy — mantiene compatibilidad con save() existente */
    suspend fun push(uid: String, payload: Map<String, Any?>) =
        doc(uid).set(payload, SetOptions.merge()).await()
}
