package com.app.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.usecase.GetCurrentStepsUseCase
import com.app.wear.domain.usecase.ObserveHeartRateUseCase
import com.app.wear.domain.usecase.SendWearableMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearMetricsViewModel @Inject constructor(
    private val observeHeartRateUseCase: ObserveHeartRateUseCase,
    private val getCurrentStepsUseCase: GetCurrentStepsUseCase,
    private val sendWearableMetricsUseCase: SendWearableMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearMetricsUiState>(WearMetricsUiState.Loading)
    val uiState: StateFlow<WearMetricsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val steps = getCurrentStepsUseCase()
            val hr = observeHeartRateUseCase().first()
            _uiState.value = WearMetricsUiState.Success(steps, hr)
        }
    }

    fun sendMetrics() {
        val state = uiState.value
        if (state is WearMetricsUiState.Success) {
            viewModelScope.launch {
                val metrics = WearableDailyMetrics(
                    steps = state.steps,
                    heartRate = state.heartRate.toFloat(),
                    caloriesBurned = null,
                    activeMinutes = null,
                    distanceMeters = null,
                    timestamp = System.currentTimeMillis(),
                    userId = null
                )
                sendWearableMetricsUseCase(metrics)
            }
        }
    }
}

sealed interface WearMetricsUiState {
    object Loading : WearMetricsUiState
    data class Success(val steps: Int, val heartRate: Int) : WearMetricsUiState
}
