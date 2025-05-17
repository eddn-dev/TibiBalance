package com.app.tibibalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.app.tibibalance.ui.navigation.AppNavGraph
import com.app.tibibalance.ui.theme.TibiBalanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TibiBalanceTheme {
                AppNavGraph()
            }
        }
    }
}