package com.app.wear.domain.usecase

import com.app.wear.domain.model.WearableHabitInfo
import com.app.wear.domain.repository.IWearHabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWearableHabitsUseCase @Inject constructor(
    private val habitRepository: IWearHabitRepository
) {
    operator fun invoke(): Flow<List<WearableHabitInfo>> {
        return habitRepository.getObservableHabits()
    }
}
