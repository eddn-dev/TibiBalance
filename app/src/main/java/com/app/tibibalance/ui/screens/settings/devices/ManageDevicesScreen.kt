// app/src/main/java/com/app/tibibalance/ui/screens/settings/devices/ManageDevicesScreen.kt
package com.app.tibibalance.ui.screens.settings.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.ui.navigation.Screen

@Composable
fun ManageDevicesScreen(
    navController: NavHostController,
    viewModel: ManageDevicesViewModel = hiltViewModel()
) {
    // Observa el estado de conexión
    val wearConnected by viewModel.wearConnected.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Administrar Dispositivos",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = { viewModel.checkWearConnection() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Verificar Wear conectado")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (wearConnected) "Wear OS conectado ✔️"
            else "Wear OS NO conectado ❌",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver")
        }
    }
}