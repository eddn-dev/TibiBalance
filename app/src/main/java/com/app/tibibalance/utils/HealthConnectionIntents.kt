/* utils/HealthConnectIntents.kt */
package com.app.tibibalance.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient

/** Abre la pantalla de ajustes de Health Connect válida para todas las versiones. */
fun Context.openHealthConnectSettings() {
    val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS) // «androidx.health.ACTION_HEALTH_CONNECT_SETTINGS»
    // Sólo la lanzamos si existe algún Activity que la resuelva
    if (intent.resolveActivity(packageManager) != null) {
        ContextCompat.startActivity(this, intent, null)
    }
}
