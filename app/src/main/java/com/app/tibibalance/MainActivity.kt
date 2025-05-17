package com.app.tibibalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.app.tibibalance.ui.navigation.AppNavGraph
import com.app.tibibalance.ui.theme.TibiBalanceTheme
import com.app.data.alert.di.ChannelInitializer      // 👈  IMPORTA el alias
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Lambda inyectada que registra los canales de notificación. */
    @Inject lateinit var initNotificationChannels: ChannelInitializer   // 👈  cambia el tipo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        initNotificationChannels()   // fun-interface admite llamada directa

        setContent {
            TibiBalanceTheme { AppNavGraph() }
        }
    }
}
