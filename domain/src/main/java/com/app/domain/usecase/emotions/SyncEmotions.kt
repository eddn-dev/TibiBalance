/* domain/usecase/emotions/SyncEmotions.kt */
package com.app.domain.usecase.emotions

import com.app.domain.repository.EmotionRepository
import javax.inject.Inject

class SyncEmotions @Inject constructor(
    private val repo: EmotionRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.syncNow()
}
