package com.app.tibibalance

import android.content.Intent // Added
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
// Removed: import com.app.data.alert.di.ChannelInitializer
import com.app.tibibalance.ui.TibiBalanceRoot
import dagger.hilt.android.AndroidEntryPoint
// Removed: import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Removed: @Inject lateinit var initNotificationChannels: ChannelInitializer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Removed: initNotificationChannels()

        // Pass the initial intent to TibiBalanceRoot
        setContent { TibiBalanceRoot(activityIntent = intent) }
    }

    @RequiresApi(Build.VERSION_CODES.O) // Ensure API level consistency if TibiBalanceRoot requires it
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Update the activity's intent
        // It's important to set the Activity's intent so that if the Activity is recreated,
        // it gets the latest intent.
        setIntent(intent) // Use setIntent(intent) to update the Activity's own intent

        // Re-compose with the new intent.
        // TibiBalanceRoot will be responsible for extracting the relevant data.
        if (intent != null) {
            setContent { TibiBalanceRoot(activityIntent = intent) }
        }
    }
}
