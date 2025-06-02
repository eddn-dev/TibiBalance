package com.app.domain.usecase.activity

import com.app.domain.repository.HabitActivityRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ActivitiesExistForDate @Inject constructor(
    private val repo: HabitActivityRepository
) {
    suspend operator fun invoke(date: LocalDate): Boolean =
        repo.observeByDate(date).first().isNotEmpty()
}



