




/**
 * @file AchievementAccessItem.kt
 * @ingroup ui_components
 * @brief Componente visual tipo botón para acceso a logros con imagen y estilo consistente.
 */

package com.app.tibibalance.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.tibibalance.ui.theme.DefaultTint
import com.app.tibibalance.ui.theme.Text
import com.app.tibibalance.R
import androidx.compose.ui.tooling.preview.Preview


/**
 * Botón visual con ícono e indicador para acceder a logros.
 *
 * @param resId ID del recurso drawable (idealmente 96x96 px o más).
 * @param label Texto mostrado al lado del ícono.
 * @param onClick Acción a ejecutar al pulsar.
 */
@Composable
fun AchievementAccessItem(
    @DrawableRes resId: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(1.dp, shape = RoundedCornerShape(12.dp))
            .background(
                color = DefaultTint,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center // CENTRA el contenido
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Icono de logros",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Text
                )
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AchievementAccessItemPreview() {
    AchievementAccessItem(
        resId = R.drawable.ic_tibio_champion,
        label = "Ver logros",
        onClick = {}
    )
}
