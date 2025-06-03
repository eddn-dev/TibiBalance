/**
 * @file    ColorPaletteLight.kt
 * @ingroup ui_theme
 * @brief   Paleta *light* para TibiBalance basada en Material 3.
 *
 * @details
 * Esta paleta sigue los roles de color de M3 (primarios, contenedores, superficies,
 * “inverse *surface*”, etc.).  Los valores se eligieron para mantener
 * contraste AA y encajar con el *branding* azul #3EA8FE.
 *
 * @note Todos los colores usan la forma ARGB: 0xFFRRGGBB.
 */

package com.app.tibibalance.ui.theme

import androidx.compose.ui.graphics.Color

/* ───────────────────────── clave de marca ───────────────────────── */
val primaryLight            = Color(0xFF3EA8FE)   ///< Azul principal (Ton-40)
val onPrimaryLight          = Color(0xFF000000)
val primaryContainerLight   = Color(0xFF72B8DE)   ///< Ton-80
val onPrimaryContainerLight = Color(0xFF000000)
val inversePrimaryLight     = Color(0xFFD0BCFF)   ///< Sugerido por M3 (Ton-80 de primary)

/* ───────────────────────── secundarios / terciarios ─────────────── */
val secondaryLight          = Color(0xFFD0CDCD)
val onSecondaryLight        = Color(0xFF000000)
val secondaryContainerLight = Color(0xFFF5FBFF)
val onSecondaryContainerLight = Color(0xFF000000)

val tertiaryLight           = Color(0xFF314954)
val onTertiaryLight         = Color(0xFF000000)
val tertiaryContainerLight  = Color(0xFF95A7D7)
val onTertiaryContainerLight = Color(0xFF000000)

/* ───────────────────────── neutrales ────────────────────────────── */
val backgroundLight         = Color(0xFFA6D1F1)
val onBackgroundLight       = Color(0xFF000000)
val surfaceLight            = Color(0xFFF3FAFF)
val onSurfaceLight          = Color(0xFF000000)
val surfaceVariantLight     = Color(0xFFEAF8FF)
val onSurfaceVariantLight   = Color(0xFF000000)
val surfaceTintLight        = primaryLight          ///< M3 usa el primario como *tint*

/*  Inverse (para *bottom sheets*, *tooltips*, etc.)  */
val inverseSurfaceLight     = Color(0xFF313033)
val inverseOnSurfaceLight   = Color(0xFFF4EFF4)   ///< ← el que faltaba

/* ───────────────────────── errores & feedback ───────────────────── */
val errorLight            = Color(0xFFB3261E)
val onErrorLight          = Color(0xFFFFFFFF)
val errorContainerLight   = Color(0xFFF5352B)
val onErrorContainerLight = Color(0xFF2D0907)
val outlineLight          = Color(0xFF79747E)
val outlineVariantLight   = Color(0xFFC4C7CF)
val scrimLight            = Color(0xFF000000)

/* ─────────────────────── superficies jerárquicas ────────────────── */
val surfaceBrightLight           = Color(0xFFF8F6F9)
val surfaceContainerLight        = Color(0xFFF2ECF4)
val surfaceContainerHighLight    = Color(0xFFECE6EE)
val surfaceContainerHighestLight = Color(0xFFE6E0E9)
val surfaceContainerLowLight     = Color(0xFFF8F2FA)
val surfaceContainerLowestLight  = Color(0xFFFFFFFF)
val surfaceDimLight              = Color(0xFFDDD8E3)
