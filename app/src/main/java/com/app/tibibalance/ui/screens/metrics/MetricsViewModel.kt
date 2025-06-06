package com.app.tibibalance.ui.screens.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.DailyMetrics
import com.app.domain.usecase.metrics.ObserveDailyMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * @file MetricsViewModel.kt
 * @brief ViewModel que expone las métricas almacenadas en Room.
 */
@HiltViewModel
class MetricsViewModel @Inject constructor(
    observeDailyMetricsUseCase: ObserveDailyMetricsUseCase
) : ViewModel() {

    /** Flujo de métricas listo para ser observado por la UI. */
    val metrics: StateFlow<List<DailyMetrics>> =
        observeDailyMetricsUseCase()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
