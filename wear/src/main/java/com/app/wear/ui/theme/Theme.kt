package com.app.wear.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * Tema Material 3 para Wear OS.
 *
 * – Sin dinámico (Monet) porque el 95 % de los relojes sigue en ≤ Android 11.
 * – ColorScheme se construye a mano: basta con dar los *core roles*;
 *   el framework rellena el resto con defaults seguros. :contentReference[oaicite:2]{index=2}
 */
@Composable
fun TibiBalanceWearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) {
        ColorScheme(
            primary            = Purple80,
            onPrimary          = Color.Black,
            secondary          = PurpleGrey80,
            tertiary           = Pink80,
            background         = Color.Black,
            onBackground       = Color.White,
        )
    } else {
        ColorScheme(
            primary            = Purple40,
            onPrimary          = Color.White,
            secondary          = PurpleGrey40,
            tertiary           = Pink40,
            background         = Color.White,
            onBackground       = Color.Black,
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography  = Typography,   // ya la tenías ajustada a Wear OS
        content     = content
    )
}
