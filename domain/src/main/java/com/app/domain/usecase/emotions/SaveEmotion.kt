/**
 * @file SaveEmotion.kt
 * @ingroup domain_usecase_emotions
 */
package com.app.domain.usecase.emotions

import com.app.domain.common.SyncMeta
import com.app.domain.entities.EmotionEntry
import com.app.domain.repository.EmotionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject


class SaveEmotion @Inject constructor(
    private val repo: EmotionRepository
) {
    suspend operator fun invoke(date: LocalDate, emojiId: String) {
        repo.upsert(
            EmotionEntry(
                date    = date,
                emojiId = emojiId,
                meta    = SyncMeta(
                    createdAt  = Clock.System.now(),
                    updatedAt  = Clock.System.now(),
                    pendingSync= true
                )
            )
        )
    }
}
