package com.app.domain.usecase.emotions

import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.app.domain.enums.Emotion                // 🆕
import com.app.domain.repository.EmotionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class SaveEmotion @Inject constructor(
    private val repo: EmotionRepository
) {
    /** Guarda o reemplaza la emoción del [date]. */
    suspend operator fun invoke(date: LocalDate, mood: Emotion) {
        val now = Clock.System.now()
        repo.upsert(
            EmotionEntry(
                date = date,
                mood = mood,
                meta = SyncMeta(
                    createdAt   = now,
                    updatedAt   = now,
                    pendingSync = true
                )
            )
        )
    }
}
