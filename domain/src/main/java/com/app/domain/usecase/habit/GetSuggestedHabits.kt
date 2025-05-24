package com.app.domain.usecase.habit

import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class GetSuggestedHabits @Inject constructor(
    private val repo: HabitRepository
) { operator fun invoke() = repo.observeSuggestedHabits() }