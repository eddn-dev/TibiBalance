package com.app.tibibalance

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory // Added
import androidx.work.Configuration // Added
import com.app.domain.enums.NotifChannel
import com.app.domain.usecase.worker.ScheduleDailyCompletionCheckUseCase // Added
import com.app.tibibalance.R
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject // Added

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
class TibiBalanceApp : Application(), Configuration.Provider { // Implemented Configuration.Provider

    @Inject lateinit var workerFactory: HiltWorkerFactory // Injected
    @Inject lateinit var scheduleDailyCompletionCheckUseCase: ScheduleDailyCompletionCheckUseCase // Injected

    override fun getWorkManagerConfiguration(): Configuration = // Implemented
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            // Optional: .setMinimumLoggingLevel(android.util.Log.DEBUG) for WorkManager debugging
            .build()

    override fun onCreate() {
        super.onCreate()

        // Activa trazas detalladas de Firestore (comenta en producción si hace ruido)
        FirebaseFirestore.setLoggingEnabled(true)
        createEssentialNotificationChannels()
        scheduleDailyCompletionCheckUseCase() // Called use case
    }

    private fun createEssentialNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Habits Channel
            val habitsChannelName = getString(R.string.notification_channel_habits_name)
            val habitsChannelDesc = getString(R.string.notification_channel_habits_description)
            val habitsChannel = NotificationChannel(
                NotifChannel.HABITS.id,
                habitsChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = habitsChannelDesc
                // TODO: Configure other properties if needed (sound, vibration default)
                // as per NotifConfig defaults if applicable, or general settings.
            }

            // TODO: Create other channels like EMOTIONS, SYSTEM if they also need to be explicitly created.
            // For now, only creating HABITS as per immediate plan requirements.

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(habitsChannel)
            // notificationManager.createNotificationChannel(emotionsChannel)
            // notificationManager.createNotificationChannel(systemChannel)
        }
    }
}