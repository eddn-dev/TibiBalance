package com.app.tibibalance

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.app.tibibalance.sync.ActivitySyncWorker
import com.app.tibibalance.sync.AppWorker
import com.app.tibibalance.sync.HabitSyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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
class TibiBalanceApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)     // usa la factory de Hilt
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("TibiBalanceApp", "onCreate")
        FirebaseFirestore.setLoggingEnabled(true)
        // AppWorker do work
        WorkManager.initialize(this, workManagerConfiguration)
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                AppWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                AppWorker.periodicRequest()
            )
        Log.d("TibiBalanceApp", "onCreate: AppWorker")

        WorkManager.getInstance(this).enqueue(AppWorker.oneShotRequest())
        Log.d("TibiBalanceApp", "onCreate: AppWorker oneShot")

        val f = HiltWorkerFactory::class.java.getDeclaredField("mWorkerFactories")
        f.isAccessible = true
        val map = f.get(workerFactory) as Map<*, *>
        Log.d("WM-FACTORY", "bindings = ${map.keys}")
    }
}