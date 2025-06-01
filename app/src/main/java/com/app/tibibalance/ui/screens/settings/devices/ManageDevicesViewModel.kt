// app/src/main/java/com/app/tibibalance/ui/screens/settings/devices/ManageDevicesViewModel.kt
package com.app.tibibalance.ui.screens.settings.devices

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManageDevicesViewModel @Inject constructor(
    private val nodeClient: NodeClient
) : ViewModel() {

    private val _wearConnected = MutableStateFlow(false)
    val wearConnected: StateFlow<Boolean> = _wearConnected

    fun checkWearConnection() {
        viewModelScope.launch {
            try {
                Log.d("WearCheck", "Iniciando búsqueda de nodos…")
                // Mover la llamada await() a un contexto de IO para no bloquear el Main Thread
                val nodes = withContext(Dispatchers.IO) {
                    Tasks.await(nodeClient.connectedNodes)
                }
                Log.d("WearCheck", "Resultado de connectedNodes: $nodes")
                _wearConnected.value = nodes.isNotEmpty()
            } catch (e: Exception) {
                Log.e("WearCheck", "Error obteniendo nodos: ${e.message}")
                _wearConnected.value = false
            }
        }
    }
}