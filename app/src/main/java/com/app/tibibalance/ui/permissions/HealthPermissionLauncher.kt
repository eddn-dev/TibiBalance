package com.app.tibibalance.ui.permissions

import androidx.activity.compose.*
import androidx.compose.runtime.*
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import kotlinx.coroutines.launch

@Composable
fun rememberHealthPermissionLauncher(
    client: HealthConnectClient,
    onResult: (Boolean) -> Unit
): ManagedActivityResultLauncher<Set<String>, Set<String>> {

    val controller = remember { client.permissionController }
    val scope      = rememberCoroutineScope()
    val contract   = remember {
        PermissionController.createRequestPermissionResultContract()
    }

    return rememberLauncherForActivityResult(contract) { newly ->
        scope.launch {
            val already = controller.getGrantedPermissions()
            val total   = already + newly
            onResult(total.containsAll(HEALTH_CONNECT_READ_PERMISSIONS))
        }
    }
}

