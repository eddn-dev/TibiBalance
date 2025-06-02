package com.app.wear.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.wear.domain.model.WearableDailyMetrics
import com.app.wear.domain.usecase.GetCurrentStepsUseCase // Necesitarás crear este caso de uso
import com.app.wear.domain.usecase.ObserveHeartRateUseCase
import com.app.wear.domain.usecase.SendWearableMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WearMetricsViewModel @Inject constructor(
    private val observeHeartRateUseCase: ObserveHeartRateUseCase,
    private val getCurrentStepsUseCase: GetCurrentStepsUseCase, // Asume que lo crearás
    private val sendWearableMetricsUseCase: SendWearableMetricsUseCase
    // @Inject val getUserIdUseCase: GetCurrentUserIdUseCase // Si necesitas el ID de usuario
) : ViewModel() {

    private val _uiState = MutableStateFlow<WearMetricsUiState>(WearMetricsUiState.Loading)
    val uiState: StateFlow<WearMetricsUiState> = _uiState.asStateFlow()

    private var currentHeartRate: Int? = null
    private var currentSteps: Int = 0


    companion object {
        private const val TAG = "WearMetricsVM"
    }

    init {
        observeSensorData()
        fetchInitialSteps()
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            observeHeartRateUseCase()
                .catch { e ->
                    Log.e(TAG, "Error observing heart rate: ${e.message}", e)
                    // Actualizar UI con error de HR si es necesario
                    // Consider emitting an error state to _uiState
                    _uiState.value = WearMetricsUiState.Error("Failed to observe heart rate: ${e.localizedMessage}")
                }
                .collect { hr ->
                    currentHeartRate = if (hr == -1 || hr == -2) null else hr // Handle error/unavailable codes from DataSource
                    updateUiState()
                }
        }
    }

    private fun fetchInitialSteps() {
        viewModelScope.launch {
            try {
                currentSteps = getCurrentStepsUseCase() // Asume que este use case existe
                updateUiState()
            } catch (e: Exception) {
                 Log.e(TAG, "Error fetching initial steps: ${e.message}", e)
                 // Consider emitting an error state to _uiState
                _uiState.value = WearMetricsUiState.Error("Failed to fetch steps: ${e.localizedMessage}")
            }
        }
    }

    private fun updateUiState() {
        // Combina los datos actuales en un estado de UI
        // Only update to Success if not already in an error state from initialization
        if (_uiState.value !is WearMetricsUiState.Error) {
             _uiState.value = WearMetricsUiState.Success(
                heartRate = currentHeartRate,
                steps = currentSteps,
                lastSentStatus = (_uiState.value as? WearMetricsUiState.Success)?.lastSentStatus ?: ""
            )
        } else {
            // If already in error, update with current data but maintain error status (or create a new error state with combined info)
            // For simplicity, we'll keep the existing error or update if new data is available
            val currentError = _uiState.value as WearMetricsUiState.Error
            _uiState.value = WearMetricsUiState.Error("HR: ${currentHeartRate ?: "N/A"}, Steps: $currentSteps. Previous error: ${currentError.message}")
        }
    }

    fun onSendMetricsClicked() {
        viewModelScope.launch {
            // Obtener el ID de usuario si es necesario y está disponible
            // val userId = getUserIdUseCase()
            val userId = "wear_user_placeholder" // Placeholder

            // Ensure we have some data to send, especially steps.
            /*// Heart rate can be null.
            if (currentSteps == 0 && currentHeartRate == null) {
                 _uiState.value = WearMetricsUiState.Success(
                    heartRate = currentHeartRate,
                    steps = currentSteps,
                    lastSentStatus = "No data to send."
                )
                Log.d(TAG, "No data to send.")
                return@launch
            }*/

            /*val metricsData = WearableDailyMetrics(
                steps = currentSteps,
                heartRate = currentHeartRate?.toFloat(),
                caloriesBurned = calculateCaloriesBurned(currentSteps), // Ejemplo de lógica
                activeMinutes = calculateActiveMinutes(currentSteps), // Ejemplo
                distanceMeters = calculateDistance(currentSteps), // Ejemplo
                timestamp = System.currentTimeMillis(),
                userId = userId
            )*/

            val metricsData = WearableDailyMetrics(
                steps = 123,
                heartRate = 70f,
                caloriesBurned = calculateCaloriesBurned(123),
                activeMinutes = calculateActiveMinutes(123),
                distanceMeters = calculateDistance(123),
                timestamp = System.currentTimeMillis(),
                userId = userId
            )

            // Show sending state
             val previousSuccessState = _uiState.value as? WearMetricsUiState.Success
            _uiState.value = WearMetricsUiState.Success(
                heartRate = currentHeartRate,
                steps = currentSteps,
                lastSentStatus = "Sending..."
            )

            val result = sendWearableMetricsUseCase(metricsData)
            val statusMessage = if (result.isSuccess) {
                "Métricas enviadas!"
            } else {
                "Error al enviar: ${result.exceptionOrNull()?.localizedMessage ?: "Desconocido"}"
            }

            _uiState.value = WearMetricsUiState.Success(
                heartRate = currentHeartRate,
                steps = currentSteps,
                lastSentStatus = statusMessage
            )
            Log.d(TAG, "Send metrics result: $statusMessage")
        }
    }

    // Funciones placeholder para cálculos (deberían ser más robustas)
    private fun calculateCaloriesBurned(steps: Int): Float = steps * 0.04f
    private fun calculateActiveMinutes(steps: Int): Int = steps / 100 // Muy simplificado
    private fun calculateDistance(steps: Int): Float = steps * 0.762f // Promedio
}

// Define los estados de la UI
sealed interface WearMetricsUiState {
    object Loading : WearMetricsUiState
    data class Success(
        val heartRate: Int?,
        val steps: Int,
        val lastSentStatus: String = ""
    ) : WearMetricsUiState
    data class Error(val message: String) : WearMetricsUiState
}
