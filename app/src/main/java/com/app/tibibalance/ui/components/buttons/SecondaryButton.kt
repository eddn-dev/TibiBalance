/**
 * @file    SecondaryButton.kt
 * @ingroup ui_component_button // Grupo específico para componentes de botón
 * @brief   Define un [Composable] para botones de acción secundaria con estilo delineado (*outlined*).
 *
 * @details Este archivo contiene el [Composable] `SecondaryButton`, diseñado para acciones
 * que tienen menor prioridad jerárquica que las acciones primarias (representadas por [PrimaryButton]).
 * Ejemplos comunes incluyen botones de "Cancelar", "Volver", "Omitir", etc.
 *
 * Utiliza el componente [OutlinedButton] de Material 3 como base, aplicándole un estilo
 * específico: fondo blanco, texto negro y un borde gris claro. Esto le da una apariencia
 * menos prominente que el botón primario.
 *
 * ### Uso típico
 * ```kotlin
 * Row {
 * SecondaryButton(
 * text    = "Cancelar",
 * onClick = { /* Lógica para cancelar */ }
 * )
 * Spacer(Modifier.width(8.dp))
 * PrimaryButton(
 * text    = "Guardar",
 * onClick = { /* Lógica para guardar */ }
 * )
 * }
 * ```
 *
 * @note
 * - Tiene una altura fija de **40dp** y un ancho mínimo fijo de **120dp** para
 * asegurar consistencia visual. A diferencia de [PrimaryButton], no ocupa
 * todo el ancho por defecto, permitiendo colocarlo junto a otros elementos.
 * - Los colores (fondo, contenido, borde) están definidos de forma explícita
 * y no dependen directamente del tema de Material 3, aunque se podrían adaptar
 * para usar colores del tema si fuera necesario.
 *
 * @see PrimaryButton Botón para acciones principales.
 * @see OutlinedButton Componente base de Material 3 utilizado internamente.
 */
package com.app.tibibalance.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size // Import necesario si se usa en previews
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.tibibalance.ui.theme.*

/**
 * @brief Un [Composable] que representa un botón de acción secundaria con estilo delineado.
 *
 * @details Utiliza [OutlinedButton] de Material 3 con un estilo visual específico
 * (fondo blanco, texto negro, borde gris claro) para indicar una acción de menor
 * prioridad. Tiene dimensiones fijas (ancho mínimo y altura) para consistencia.
 * Maneja el estado `enabled` para la interacción.
 *
 * @param text El [String] que se muestra como etiqueta del botón.
 * @param onClick La función lambda que se ejecuta cuando el usuario pulsa el botón (si está habilitado).
 * @param modifier Un [Modifier] opcional para aplicar personalizaciones de layout adicionales
 * (e.g., padding externo). Por defecto, no aplica modificaciones adicionales más allá
 * del tamaño fijo interno.
 * @param enabled Un [Boolean] que controla si el botón está habilitado para la interacción.
 * Por defecto `true`.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier, // Modificador externo opcional
    enabled: Boolean = true // Habilitado por defecto
) {
    OutlinedButton(
        onClick  = onClick, // Acción al pulsar
        enabled  = enabled, // Estado de habilitación
        modifier = modifier // Aplica modificador externo PRIMERO
            .width(120.dp) // Ancho mínimo
            .height(48.dp), // Altura fija (ajustada a 48dp)
        shape  = RoundedCornerShape(12.dp), // Bordes redondeados
        colors = ButtonDefaults.outlinedButtonColors( // Colores para OutlinedButton
            // Estado normal
            containerColor         = MaterialTheme.colorScheme.secondaryContainer, // Fondo blanco
            contentColor           = MaterialTheme.colorScheme.onSecondaryContainer, // Texto negro
            // Estado deshabilitado
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), // Fondo gris claro semitransparente
            disabledContentColor   = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f) // Texto gris claro semitransparente
        ),
    ) {
        // Contenido del botón (el texto)
        Text(
            text  = text, // Etiqueta del botón
            style = MaterialTheme.typography.labelLarge.copy( // Estilo de texto (usando labelLarge)
                fontWeight = FontWeight.SemiBold
                // El color se hereda de contentColor definido en ButtonDefaults
            )
        )
    }
}

/* ─────────────────────────── Previews ─────────────────────────── */

/**
 * @brief Previsualización del [SecondaryButton] en estado normal (habilitado).
 */
@Preview(showBackground = true, name = "Secondary Button Enabled")
@Composable
private fun PreviewSecondaryButton() {
    MaterialTheme { // Envuelve en MaterialTheme para estilos de texto correctos
        SecondaryButton(text = "Cancelar", onClick = {})
    }
}

/**
 * @brief Previsualización del [SecondaryButton] en estado deshabilitado.
 */
@Preview(showBackground = true, name = "Secondary Button Disabled")
@Composable
private fun PreviewSecondaryButtonDisabled() {
    MaterialTheme {
        SecondaryButton(text = "Volver", onClick = {}, enabled = false)
    }
}