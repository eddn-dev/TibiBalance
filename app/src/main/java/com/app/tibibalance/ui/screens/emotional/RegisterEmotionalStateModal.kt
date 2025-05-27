// src/main/java/com/app/tibibalance/ui/screens/emotional/RegisterEmotionalStateModal.kt
package com.app.tibibalance.ui.screens.emotional

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text // Keep Text for potential future use if needed, but not directly used for emotion label now.
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.buttons.EmotionButton
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.texts.Subtitle
import java.time.LocalDate
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme // Still useful for general styling

/**
 * @file    RegisterEmotionalStateModal.kt
 * @ingroup ui_screens_emotional
 * @brief   Diálogo para escoger y confirmar la emoción de un día dado.
 *
 * @param date        Fecha a la que se está asignando la emoción.
 * @param onDismiss   Callback que se invoca al cerrar el modal sin confirmar.
 * @param onConfirm   Callback que recibe la [Emotion] seleccionada cuando el usuario pulsa "LISTO".
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegisterEmotionalStateModal(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (Emotion) -> Unit
) {
    // Emociones disponibles en orden deseado para la cuadrícula
    val emotions = listOf(
        Emotion.FELICIDAD,
        Emotion.TRANQUILIDAD,
        Emotion.TRISTEZA,
        Emotion.ENOJO,
        Emotion.DISGUSTO,
        Emotion.MIEDO
    )

    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }

    ModalContainer(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier           = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment= Alignment.CenterHorizontally,
            verticalArrangement= Arrangement.spacedBy(16.dp)
        ) {
            // Título con la fecha
            Subtitle(
                text      = "Selecciona la emoción que más resonó contigo en ${date.dayOfMonth}/${date.monthValue}/${date.year}",
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            // Cuadrícula de emociones
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columnas para una cuadrícula
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), // Ajusta la altura al contenido
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Espacio horizontal entre elementos
                verticalArrangement = Arrangement.spacedBy(5.dp) // Espacio vertical entre elementos
            ) {
                items(emotions) { emotion ->
                    // El EmotionButton ya debería incluir la imagen y el texto de la emoción.
                    // Si EmotionButton solo muestra la imagen, y necesitas el texto debajo,
                    // tendríamos que modificar EmotionButton o crear un composable nuevo.
                    // Asumo que EmotionButton es el que debe mostrar el label.
                    EmotionButton(
                        emotionLabel = emotion.label, // El texto se pasa aquí
                        emotionRes   = emotion.drawableRes,
                        isSelected   = selectedEmotion == emotion,
                        size         = 100.dp, // Tamaño fijo y más grande para el botón de emoción
                        onClick      = {
                            selectedEmotion = emotion
                        },
                        // Puedes añadir un Modifier aquí si necesitas padding o tamaño específico
                        // para el contenedor del EmotionButton dentro de la celda de la cuadrícula
                        modifier = Modifier
                            .fillMaxWidth() // Para que ocupe el ancho de la celda
                            .padding(4.dp) // Pequeño padding alrededor del botón
                    )
                    // Eliminado el Text duplicado aquí.
                    // Si tu EmotionButton no muestra el texto, entonces el problema está ahí.
                }
            }

            // Botón de confirmación
            PrimaryButton(
                text    = "LISTO",
                enabled = selectedEmotion != null,
                onClick = {
                    selectedEmotion?.let(onConfirm)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
    }
}


/**
 * @brief   Tipos de emoción disponibles.
 * @param drawableRes Drawable asociado.
 * @param label       Texto descriptivo.
 */
enum class Emotion(val drawableRes: Int, val label: String) {
    FELICIDAD    (R.drawable.ic_happyimage,    "Felicidad"),
    TRANQUILIDAD (R.drawable.ic_calmimage,     "Calma"),
    TRISTEZA     (R.drawable.ic_sadimage,      "Tristeza"),
    ENOJO        (R.drawable.ic_angryimage,    "Enojo"),
    DISGUSTO     (R.drawable.ic_disgustingimage,  "Disgusto"),
    MIEDO        (R.drawable.ic_fearimage,     "Miedo");
}