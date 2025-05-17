package com.app.tibibalance

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp

/**
 * @file      TibiBalanceApp.kt
 * @ingroup   app_bootstrap
 * @brief     Punto de entrada de la aplicación.  Inicializa Hilt y configura
 *            servicios globales (p.-ej., Firestore logging).
 *
 * @details
 *  - Anotamos con **@HiltAndroidApp** para que Dagger-Hilt genere el grafo.
 *  - Activamos el log VERBOSE de Firestore para facilitar depuración en Logcat.
 */
@HiltAndroidApp
class TibiBalanceApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Activa trazas detalladas de Firestore (comenta en producción si hace ruido)
        FirebaseFirestore.setLoggingEnabled(true)
    }
}