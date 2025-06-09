// ui/screens/emotional/RegisterEmotionalStateModal.kt
package com.app.tibibalance.ui.screens.emotional

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.domain.enums.Emotion
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.EmotionButton
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.texts.Subtitle
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegisterEmotionalStateModal(
    date: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (Emotion) -> Unit
) {
    /* 1️⃣  Catálogo de emociones en el orden deseado (6 celdas) */
    val emotions = listOf(
        Emotion.FELICIDAD,
        Emotion.TRANQUILIDAD,
        Emotion.TRISTEZA,
        Emotion.ENOJO,
        Emotion.DISGUSTO,
        Emotion.MIEDO
    )

    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }

    /* 2️⃣  Contenedor modal */
    ModalContainer(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* Título con la fecha */
            Subtitle(
                text      = "Selecciona la emoción que más resonó contigo el " +
                        "${date.dayOfMonth}/${date.monthValue}/${date.year}",
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            /* 3️⃣  Cuadrícula 3 × 2 de emociones */
            LazyVerticalGrid(
                columns               = GridCells.Fixed(3),
                modifier              = Modifier.wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp)
            ) {
                items(emotions) { emo ->
                    EmotionButton(
                        emotionLabel = emo.label,
                        emotionRes   = emo.drawableRes,
                        isSelected   = selectedEmotion == emo,
                        size         = 92.dp,
                        onClick      = { selectedEmotion = emo },
                        modifier     = Modifier.fillMaxWidth()
                    )
                }
            }

            /* 4️⃣  Botón LISTO */
            PrimaryButton(
                text    = "LISTO",
                enabled = selectedEmotion != null,
                onClick = { selectedEmotion?.let(onConfirm) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------------- */
/*  Extensiones utilitarias para Emotion                                      */
/* ------------------------------------------------------------------------- */

val Emotion.label: String
    get() = when (this) {
        Emotion.FELICIDAD    -> "Feliz"
        Emotion.TRANQUILIDAD -> "Tranquilo"
        Emotion.TRISTEZA     -> "Triste"
        Emotion.ENOJO        -> "Enojado"
        Emotion.DISGUSTO     -> "Disgustado"
        Emotion.MIEDO        -> "Asustado"
    }

@get:DrawableRes
val Emotion.drawableRes: Int
    get() = when (this) {
        Emotion.FELICIDAD    -> R.drawable.ic_happyimage
        Emotion.TRANQUILIDAD -> R.drawable.ic_calmimage
        Emotion.TRISTEZA     -> R.drawable.ic_sadimage
        Emotion.ENOJO        -> R.drawable.ic_angryimage
        Emotion.DISGUSTO     -> R.drawable.ic_disgustingimage
        Emotion.MIEDO        -> R.drawable.ic_fearimage
    }
