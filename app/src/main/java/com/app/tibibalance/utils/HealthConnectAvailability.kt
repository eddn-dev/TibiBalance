/* utils/HealthConnectAvailability.kt */

package com.app.tibibalance.utils

import android.content.Context
import android.os.Build
import androidx.annotation.IntDef
import androidx.health.connect.client.HealthConnectClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper estático para saber si Health Connect está listo para usarse.
 *
 * Uso:
 * ```kotlin
 * val hcAvailable = hcAvailability.isHealthConnectReady()
 * if (hcAvailable) { /* pedir permisos / leer métricas */ }
 * ```
 *
 * @constructor Se inyecta un `Context` de aplicación para que sea seguro usarlo
 *              desde ViewModels o Workers.
 *
 * @author  Edd
 * @version 1.0
 *
 * @see HealthConnectClient
 */
@Singleton
class HealthConnectAvailability @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        /** Paquete oficial de la app Health Connect en Play Store. */
        private const val GOOGLE_PROVIDER = "com.google.android.apps.healthdata"

        /** Posibles resultados de disponibilidad (alias de las constantes del SDK). */
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(
            HealthConnectClient.SDK_AVAILABLE,
            HealthConnectClient.SDK_UNAVAILABLE,
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
        )
        annotation class SdkStatus
    }

    /** Devuelve true sólo si el SDK está listo para usarse sin crashear. */
    fun isHealthConnectReady(): Boolean =
        getStatus() == HealthConnectClient.SDK_AVAILABLE


    /** Devuelve el estado bruto que expone `HealthConnectClient#getSdkStatus()`. */
    @SdkStatus
    fun getStatus(): Int =
        HealthConnectClient.getSdkStatus(context, GOOGLE_PROVIDER)
}
