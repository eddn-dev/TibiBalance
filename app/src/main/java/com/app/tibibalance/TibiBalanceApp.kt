// app/src/main/java/com/app/tibibalance/TibiBalanceApp.kt
package com.app.tibibalance

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.app.data.worker.MetricsSyncWorker
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Punto de entrada de la aplicación.
 *
 * - Con la anotación **@HiltAndroidApp** Hilt genera el grafo de dependencias
 *   e inyecta el `HiltWorkerFactory` antes de que WorkManager solicite la
 *   configuración.
 * - `TibiBalanceApp` implementa `Configuration.Provider` usando **el método
 *   getWorkManagerConfiguration()**, compatible con WorkManager 2.8.x.
 */
@HiltAndroidApp
class TibiBalanceApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        FirebaseFirestore.setLoggingEnabled(true)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<MetricsSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MetricsSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }

    /** API WorkManager 2.9+ → propiedad, no método */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
