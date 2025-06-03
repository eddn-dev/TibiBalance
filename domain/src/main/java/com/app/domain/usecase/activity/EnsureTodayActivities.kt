package com.app.domain.usecase.activity

import kotlinx.datetime.LocalDate
import javax.inject.Inject

class EnsureTodayActivities @Inject constructor(
    private val exists  : ActivitiesExistForDate,
    private val generate: GenerateDailyActivities
) {
    suspend operator fun invoke(today: LocalDate) {
        if (!exists(today)) generate(today)
    }
}
