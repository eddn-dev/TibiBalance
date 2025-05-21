/**
 * @file    ObserveEmotions.kt
 * @ingroup domain_usecase_emotions
 */
package com.app.domain.usecase.emotions

import com.app.domain.entities.EmotionEntry
import com.app.domain.repository.EmotionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEmotions @Inject constructor(
    private val repo: EmotionRepository
) {
    operator fun invoke(): Flow<List<EmotionEntry>> = repo.observeAll()
}