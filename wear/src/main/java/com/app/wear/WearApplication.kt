package com.app.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Quitamos TODO lo relacionado con FirebaseAuth y Firestore
    }
}
