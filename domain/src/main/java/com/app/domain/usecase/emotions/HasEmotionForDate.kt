// :domain/usecase/emotion/HasEmotionEntryForDate.kt
package com.app.domain.usecase.emotion

import com.app.domain.repository.EmotionRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class HasEmotionEntryForDate @Inject constructor(
    private val repo: EmotionRepository
) { suspend operator fun invoke(date: LocalDate) = repo.hasEntryFor(date) }
