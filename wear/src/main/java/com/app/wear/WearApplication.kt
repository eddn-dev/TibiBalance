package com.app.wear

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WearApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("WearApp", "WearApplication inicializada sin Firebase")
        // Aquí, si quisieras, puedes inicializar Data Layer, WorkManager, etc.
    }
}
