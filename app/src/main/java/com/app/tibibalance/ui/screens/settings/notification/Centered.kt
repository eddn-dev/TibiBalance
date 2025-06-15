package com.app.tibibalance.ui.screens.settings.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.tibibalance.ui.components.texts.Description


/* helper para mensajes centrados */
@Composable
fun Centered(msg: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Description(text = msg)
    }

