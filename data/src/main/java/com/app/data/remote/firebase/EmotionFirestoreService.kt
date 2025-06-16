/**
 * @file    EmotionFirestoreService.kt
 * @ingroup data_remote
 * @brief   Wrapper tipado sobre la colección `users/{uid}/emotions`.
 */
package com.app.data.remote.firebase

import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.app.domain.enums.Emotion                    // 🆕
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

            /* ⚠️  Compatibilidad: primero intenta “mood”, si no “emojiId” */
            val moodStr = doc.getString("mood") ?: doc.getString("emojiId")
            ?: return@mapNotNull null
            val mood = runCatching { Emotion.valueOf(moodStr) }.getOrNull()
                ?: return@mapNotNull null         // ignora registros corruptos

            EmotionEntry(
                date = LocalDate.parse(dateStr),
                mood = mood,
                meta = SyncMeta(
                    createdAt   = doc.getString("createdAt")?.let(Instant::parse)
                        ?: Instant.DISTANT_PAST,
                    updatedAt   = doc.getString("updatedAt")?.let(Instant::parse)
                        ?: Instant.DISTANT_PAST,
                    deletedAt   = doc.getString("deletedAt")?.let(Instant::parse),
                    pendingSync = false            // viene de Firestore
                )
            )
        }
    }

    /* ───── escrituras ────────────────────────── */

    /** Sube/actualiza un registro emocional (idempotente). */
    suspend fun push(uid: String, entry: EmotionEntry) {
        val payload = mapOf(
            "date"      to entry.date.toString(),
            "mood"      to entry.mood.name,        // ← enum → String
            // Campo legado para apps antiguas (opcional; elimina si no lo necesitas)
            "emojiId"   to entry.mood.name,
            "createdAt" to entry.meta.createdAt.toString(),
            "updatedAt" to entry.meta.updatedAt.toString(),
            "deletedAt" to entry.meta.deletedAt?.toString()
        )

        col(uid).document(entry.date.toString())
            .set(payload, SetOptions.merge())      // merge evita pisar campos futuros
            .await()
    }
}
