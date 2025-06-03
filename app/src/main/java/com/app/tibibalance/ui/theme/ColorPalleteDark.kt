// ColorPaletteDark.kt (versión ajustada)

package com.app.tibibalance.ui.theme

import androidx.compose.ui.graphics.Color

/* ─────────── Color de marca (azules muy oscuros) ─────────── */
val primaryDark            = Color(0xFF1957B2)   // Índigo muy oscuro (antes: 0xFF1B71C9)
/* Texto sobre primary */
val onPrimaryDark          = Color(0xFFFFFFFF)   // Blanco puro

/* Container del primary, un tono aún más oscuro */
val primaryContainerDark   = Color(0xFF082E7B)   // Índigo/antracita, para botones secundarios o contenedores
val onPrimaryContainerDark = Color(0xFFB9D9FF)   // Permanece claro para texto, funciona sobre primaryContainerDark

/* Inverso primario (antes era azul claro, ahora lo trasladamos al mismo tono índigo oscuro) */
val inversePrimaryDark     = Color(0xFF0D47A1)   // Igual que primaryDark (no hay “azul claro”)

/* ─────────── Secundarios / Terciarios (neutros/grises) ────────── */
// He convertido el tono terciario a un gris oscuro, para evitar cualquier azul “vistoso”.
val secondaryDark          = Color(0xFFB8B5BC)   // Neutro frío claro
val onSecondaryDark        = Color(0xFF201F24)   // Texto sobre secondaryDark
val secondaryContainerDark = Color(0xFF5E5C63)   // Gris antracita claro
val onSecondaryContainerDark = Color(0xFFDCD9E0) // Texto sobre secondaryContainerDark

/* En vez de un azul en tertiary, usamos gris humo oscuro */
val tertiaryDark           = Color(0xFF5E5E5E)   // Gris medio/oscuro
val onTertiaryDark         = Color(0xFFF1F1F1)   // Blanco sucio, para contraste
val tertiaryContainerDark  = Color(0xFF424242)   // Gris oscuro (container)
val onTertiaryContainerDark = Color(0xFFE0E0E0)  // Gris claro para texto

/* ─────────── Neutrales (background & surfaces) ───────────── */
// Fondo degradado: gris-azulado medio → gris-oscuro, sin tonos azules vivos
val backgroundDark         = Color(0xFF2E374D)   // Gris-azulado medio
val onBackgroundDark       = Color(0xFFE3E2E6)   // Texto casi blanco

val surfaceDark            = Color(0xFF23282F)   // Gris oscuro con ligero matiz frío
val onSurfaceDark          = Color(0xFFC7C6CA)   // Texto blanco grisáceo

val surfaceVariantDark     = Color(0xFF3A4050)   // Gris-pizarra medio
val onSurfaceVariantDark   = Color(0xFFC4C7CF)   // Texto sobre la variante

/* Tint para overlays de elevación: ahora el mismo índigo oscuro */
val surfaceTintDark        = primaryDark

/*  Inverse (snackbars, tooltips, etc.)  */
val inverseSurfaceDark     = Color(0xFFF4EFF4)   // Gris muy claro
val inverseOnSurfaceDark   = Color(0xFF313033)   // Texto sobre inverseSurfaceDark

/* ─────────── Feedback (errores, outline, scrim) ──────────── */
// Error ahora es un rojo más apagado/muteado, no tan brillante
val errorDark            = Color(0xFFB85F5B)   // Rojo “rojizo suave”
// Texto sobre el error
val onErrorDark          = Color(0xFFFFFFFF)   // Blanco puro

// Container de error ligeramente más oscuro que errorDark
val errorContainerDark   = Color(0xFF5F1F1E)   // Rojo muy oscuro/muteado
val onErrorContainerDark = Color(0xFFF9DEDC)   // Texto semiblanco

val outlineDark          = Color(0xFF938F99)   // Continúa siendo un gris frío neutro
val outlineVariantDark   = Color(0xFF44464F)   // Bordes más oscuros
val scrimDark            = Color(0xFF000000)   // Negro puro (con alfa aplicado dinámicamente)

/* ──────── Surfaces jerárquicas (tone-based) ─────────────── */
val surfaceBrightDark           = Color(0xFF353A43)   // Un grado más claro que surfaceVariantDark
val surfaceContainerDark        = Color(0xFF23282F)   // Igual que surfaceDark
val surfaceContainerHighDark    = Color(0xFF2A2F38)   // Intermedio, para tarjetas elevadas
val surfaceContainerHighestDark = Color(0xFF303743)   // Para bottom sheets o diálogos
val surfaceContainerLowDark     = Color(0xFF1E2228)   // Un peldaño debajo de surfaceDark
val surfaceContainerLowestDark  = Color(0xFF191C21)   // Muy oscuro, fondo de modal full-screen
val surfaceDimDark              = Color(0xFF1C1F24)   // Gris casi negro con leve matiz frío