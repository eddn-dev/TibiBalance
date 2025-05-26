/* MainActivity.kt */
package com.app.tibibalance

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.app.data.alert.di.ChannelInitializer
import com.app.tibibalance.ui.TibiBalanceRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var initNotificationChannels: ChannelInitializer

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        initNotificationChannels()

        setContent { TibiBalanceRoot() }            // ⬅️
    }
}
