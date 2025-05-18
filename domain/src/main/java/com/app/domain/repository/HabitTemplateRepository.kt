// :domain/src/main/java/com/app/domain/repository/HabitTemplateRepository.kt
package com.app.domain.repository

import com.app.domain.entities.HabitTemplate
import kotlinx.coroutines.flow.Flow

interface HabitTemplateRepository {
    val templates: Flow<List<HabitTemplate>>
    suspend fun refreshOnce()
    fun startSync()           // the scope is chosen inside impl
}
