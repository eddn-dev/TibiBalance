/* domain/usecase/habit/SyncHabits.kt */
package com.app.domain.usecase.habit

import com.app.domain.repository.HabitRepository
import javax.inject.Inject

class SyncHabits @Inject constructor(
    private val repo: HabitRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.syncNow()
}
