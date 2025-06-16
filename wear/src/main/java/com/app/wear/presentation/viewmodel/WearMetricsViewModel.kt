/**
 * @file WearMetricsViewModel.kt
 * @brief Lógica de UI: pasos + BPM diario + BPM en tiempo real + envío Data Layer.
 */
package com.app.wear.presentation.viewmodel

import androidx.health.services.client.PassiveListenerCallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wear.data.provider.hc.HcHeartRateProvider
import com.app.wear.data.provider.hc.HcStepProvider
import com.app.wear.data.datasource.WearableApiCommDataSource
import com.app.wear.domain.model.DailyMetricsPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.health.services.client.PassiveMonitoringClient
import androidx.health.services.client.data.*
import kotlin.math.roundToInt

@HiltViewModel
class WearMetricsViewModel @Inject constructor(
    private val stepProvider: HcStepProvider,
    private val hrProvider: HcHeartRateProvider,
    private val passiveClient: PassiveMonitoringClient,
    private val comm: WearableApiCommDataSource
) : ViewModel(), PassiveListenerCallback {

    /* ─── State ─── */
    private val _state = MutableStateFlow(MetricsUiState())
    val state: StateFlow<MetricsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    init {
        observeRealtimeBpm()
        refreshAggregates()          // primer pintado
    }

    /* ─── 1. BPM en directo ─── */
    private fun observeRealtimeBpm() {
        val cfg = PassiveListenerConfig.builder()
            .setDataTypes(setOf(DataType.HEART_RATE_BPM))
            .build()                                           // config oficial:contentReference[oaicite:7]{index=7}

        viewModelScope.launch {
            passiveClient.setPassiveListenerCallback(cfg, this@WearMetricsViewModel)
        }
    }

    override fun onNewDataPointsReceived(container: DataPointContainer) {
        container
            .getData(DataType.HEART_RATE_BPM)               // ← lista tipada
            .lastOrNull()                                   // último dato recibido
            ?.let { dp: SampleDataPoint<Double> ->
                val bpm = dp.value.roundToInt()             // value es Double
                _state.update { it.copy(heartRate = bpm) }
            }
    }


    /* ─── 2. Agregados diarios ─── */
    private fun refreshAggregates() = viewModelScope.launch {
        val steps = stepProvider.todaySteps()                  // Health Connect aggregate:contentReference[oaicite:8]{index=8}
        val bpmAvg = hrProvider.todayAvgBpm()
        _state.update { it.copy(steps = steps, heartRate = bpmAvg) }
    }

    /* ─── 3. Envío Data Layer ─── */
    fun onSendMetricsClicked() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, lastSentStatus = null) }
            val steps = stepProvider.todaySteps()
            val bpmDaily = hrProvider.todayAvgBpm()
            val payload = DailyMetricsPayload(steps, bpmDaily)

            comm.sendMetricsPayload(payload)                  // setUrgent & await:contentReference[oaicite:9]{index=9}
                .onSuccess {
                    _state.update { it.copy(isLoading = false, lastSentStatus = "Enviado ✔") }
                    _events.emit(UiEvent.Toast("Métricas enviadas"))
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e) }
                    _events.emit(UiEvent.Toast("Error: ${e.message}"))
                }
        }
    }

    /* ─── callbacks vacíos obligatorios ─── */
    fun onAccuracyChanged(dataType: DataType<*, *>, accuracy: DataPointAccuracy?) {}
    fun onFlushComplete(requestId: Int) {}
    fun onDataCollectionEnabledChanged(enabled: Boolean) {}
}
