package com.app.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.app.wear.presentation.ui.WearAppScreen
import com.app.wear.ui.theme.TibiBalanceWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TibiBalanceWearTheme {
                WearAppScreen()
            }
        }
    }
}