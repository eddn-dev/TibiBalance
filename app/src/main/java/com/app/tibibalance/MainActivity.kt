/* MainActivity.kt */
package com.app.tibibalance

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.data.alert.di.ChannelInitializer
import com.app.tibibalance.ui.TibiBalanceRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var initNotificationChannels: ChannelInitializer

    /* launcher para la petición */
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // si el usuario dice «no», tu app seguirá funcionando, solo sin avisos
        if (!granted) Log.d("NotifPerm", "POST_NOTIFICATIONS denegado")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        initNotificationChannels()

        /* ---- pedir permiso en Android 13 + ---- */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val already = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!already) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent { TibiBalanceRoot() }
    }
}

