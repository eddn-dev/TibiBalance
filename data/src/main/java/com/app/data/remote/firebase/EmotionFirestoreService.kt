/**
 * @file    EmotionFirestoreService.kt
 * @ingroup data_remote
 * @brief   Wrapper tipado sobre la colección `users/{uid}/emotions`.
 */
package com.app.data.remote.firebase

import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class EmotionFirestoreService @Inject constructor(
    private val db: FirebaseFirestore
) {

    /* ───── helpers ───────────────────────────── */
    private fun col(uid: String) =
        db.collection("users").document(uid).collection("emotions")

    /* ───── lecturas ──────────────────────────── */

    /** Devuelve la lista completa de registros emocionales del usuario. */
    suspend fun fetchAll(uid: String): List<EmotionEntry> {
        val snap = col(uid).get().await()
        return snap.documents.mapNotNull { doc ->
            val dateStr = doc.getString("date") ?: return@mapNotNull null
            val emojiId = doc.getString("emojiId") ?: return@mapNotNull null

            EmotionEntry(
                date    = LocalDate.parse(dateStr),
                emojiId = emojiId,
                meta = SyncMeta(
                    createdAt  = doc.getString("createdAt")?.let(Instant::parse)
                        ?: Instant.DISTANT_PAST,
                    updatedAt  = doc.getString("updatedAt")?.let(Instant::parse)
                        ?: Instant.DISTANT_PAST,
                    deletedAt  = doc.getString("deletedAt")?.let(Instant::parse),
                    pendingSync = false          // viene de Firestore → ya sincronizado
                )
            )
        }
    }

    /* ───── escrituras ────────────────────────── */

    /** Sube/actualiza un registro emocional. */
    suspend fun push(uid: String, entry: EmotionEntry) {
        val payload = mapOf(
            "date"      to entry.date.toString(),
            "emojiId"   to entry.emojiId,
            "createdAt" to entry.meta.createdAt.toString(),
            "updatedAt" to entry.meta.updatedAt.toString(),
            "deletedAt" to entry.meta.deletedAt?.toString()
        )
        col(uid).document(entry.date.toString())
            .set(payload, SetOptions.merge())           // merge evita pisar campos futuros
            .await()
    }
}
