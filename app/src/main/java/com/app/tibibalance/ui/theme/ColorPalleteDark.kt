/**
 * @file    ColorPaletteDark.kt
 * @ingroup ui_theme
 * @brief   Paleta *dark* para TibiBalance (Material 3 + branding azul).
 *
 * @details
 *  – Todos los valores están en formato ARGB 0xFFRRGGBB.
 *  – Los tonos siguen la escala de Material 3 (tone-based surfaces y roles).
 *  – Se procuró contraste AA y coherencia con la paleta *light* que ya generamos.
 *
 *  Referencias:
 *    • Roles y tonos   –  Material 3 docs :contentReference[oaicite:0]{index=0}
 *    • Dynamic/dark    –  Material 3 “Static & Dynamic color” :contentReference[oaicite:1]{index=1}
 *    • Design tokens   –  Material 3 tokens overview :contentReference[oaicite:2]{index=2}
 *    • Contraste AA    –  Dark-theme guidance :contentReference[oaicite:3]{index=3}
 *    • Scrim & sheets  –  Bottom-sheet spec (scrim) :contentReference[oaicite:4]{index=4}
 */

package com.app.tibibalance.ui.theme

import androidx.compose.ui.graphics.Color

/* ─────────── Color de marca (azules “mid-night”) ─────────── */
val primaryDark            = Color(0xFF1B71C9)   ///< Tone 60 – azul principal
val onPrimaryDark          = Color(0xFFFFFFFF)
val primaryContainerDark   = Color(0xFF004B8E)   ///< Tone 30
val onPrimaryContainerDark = Color(0xFFB9D9FF)   ///< Texto en contenedor

/* El inverso de primary para *surface* elevadas */
val inversePrimaryDark     = Color(0xFF3EA8FE)   ///< Re-usa tu azul claro

/* ─────────── Secundarios / Terciarios (tono sutil) ────────── */
val secondaryDark          = Color(0xFFB8B5BC)   ///< Neutro frío
val onSecondaryDark        = Color(0xFF201F24)
val secondaryContainerDark = Color(0xFF6C6C6C)
val onSecondaryContainerDark = Color(0xFFDCD9E0)

val tertiaryDark           = Color(0xFF7BA5C4)
val onTertiaryDark         = Color(0xFF002138)
val tertiaryContainerDark  = Color(0xFF00395B)
val onTertiaryContainerDark = Color(0xFFBFD9F3)

/* ─────────── Neutrales (background & surfaces) ───────────── */
val backgroundDark         = Color(0xFF151B2A)   ///< M3 tone 6
val onBackgroundDark       = Color(0xFFE3E2E6)   ///< M3 tone 90
val surfaceDark            = Color(0xFF1A1C1E)
val onSurfaceDark          = Color(0xFFC7C6CA)
val surfaceVariantDark     = Color(0xFF44464F)
val onSurfaceVariantDark   = Color(0xFFC4C7CF)

/* Tint = primary para *elevation overlays* */
val surfaceTintDark        = primaryDark

/*  Inverse (para *snackbars*, *tooltips*, etc.)  */
val inverseSurfaceDark     = Color(0xFFF4EFF4)   ///< tone 95 :contentReference[oaicite:5]{index=5}
val inverseOnSurfaceDark   = Color(0xFF313033)

/* ─────────── Feedback (errores, outline, scrim) ──────────── */
val errorDark            = Color(0xFFF2B8B5)   ///< baseline dark error :contentReference[oaicite:6]{index=6}
val onErrorDark          = Color(0xFF601410)
val errorContainerDark   = Color(0xFF8C1D18)   ///< tone 30 :contentReference[oaicite:7]{index=7}
val onErrorContainerDark = Color(0xFFF9DEDC)
val outlineDark          = Color(0xFF938F99)   ///< tone 60 :contentReference[oaicite:8]{index=8}
val outlineVariantDark   = Color(0xFF44464F)   ///< tone 30
val scrimDark            = Color(0xFF000000)   ///< 60 % alpha manejado por M3 :contentReference[oaicite:9]{index=9}

/* ──────── Surfaces jerárquicas (tone-based) ─────────────── */
val surfaceBrightDark           = Color(0xFF35383B)   ///< tone 24
val surfaceContainerDark        = Color(0xFF1F2326)   ///< tone 12
val surfaceContainerHighDark    = Color(0xFF262A2D)   ///< tone 17
val surfaceContainerHighestDark = Color(0xFF2F3336)   ///< tone 22
val surfaceContainerLowDark     = Color(0xFF14171A)   ///< tone 8
val surfaceContainerLowestDark  = Color(0xFF0D0E11)   ///< tone 4
val surfaceDimDark              = Color(0xFF121316)   ///< tone 6
