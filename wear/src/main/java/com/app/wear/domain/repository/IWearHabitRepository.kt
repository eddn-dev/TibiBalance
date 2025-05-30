package com.app.wear.domain.repository

import com.app.wear.domain.model.WearableHabitInfo
import kotlinx.coroutines.flow.Flow

// Interfaz para el repositorio que maneja la información de hábitos en el wearable.
interface IWearHabitRepository {
    // Obtiene un flujo de la lista de hábitos relevantes para mostrar en el wearable.
    // Estos datos podrían ser sincronizados desde la app móvil.
    fun getObservableHabits(): Flow<List<WearableHabitInfo>>

    // Marca un hábito como completado/no completado desde el wearable y notifica a la app.
    suspend fun toggleHabitCompletion(habitId: String, isCompleted: Boolean): Result<Unit>
}
