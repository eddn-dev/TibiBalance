package com.app.wear

import android.Manifest
import android.content.pm.PackageManager           // ①
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts // ②
import androidx.core.content.ContextCompat         // ③
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import com.app.wear.presentation.WearAppScreen
import com.app.wear.ui.theme.TibiBalanceWearTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ─── 1. Permisos de sensores (sistema) ───
    private val sensorPerms = buildList {
        add(Manifest.permission.ACTIVITY_RECOGNITION)           // pasos:contentReference[oaicite:3]{index=3}
        if (Build.VERSION.SDK_INT >= 34)                        // Wear OS 6+
            add("android.permission.READ_HEART_RATE")           // granular HR:contentReference[oaicite:4]{index=4}
        else
            add(Manifest.permission.BODY_SENSORS)
    }.toTypedArray()

    // ─── 2. Permisos de Health Connect ───
    private val hcPerms = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    private lateinit var sensorLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var hcLauncher: ActivityResultLauncher<Set<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* ① Sensores */
        sensorLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* opcional: analiza resultados */ }

        if (!sensorPerms.allGranted()) sensorLauncher.launch(sensorPerms)

        /* ② Health Connect */
        hcLauncher = registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted: Set<String> ->
            if (!granted.containsAll(hcPerms)) {
                // el usuario negó algo → feedback
            }
        }

        val hcClient = HealthConnectClient.getOrCreate(this)
        lifecycleScope.launch {
            val granted = hcClient.permissionController.getGrantedPermissions()  // sin args:contentReference[oaicite:7]{index=7}
            if (!granted.containsAll(hcPerms)) hcLauncher.launch(hcPerms)
        }

        /* ③ UI */
        setContent { TibiBalanceWearTheme { WearAppScreen() } }
    }

    private fun Array<String>.allGranted() =
        all { ContextCompat.checkSelfPermission(this@MainActivity, it) ==
                PackageManager.PERMISSION_GRANTED }
}

