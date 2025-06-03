package com.app.tibibalance.ui.components.inputs

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.tibibalance.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantitySlider(
    qty: Int,
    onQtyChange: (Int) -> Unit,
    target: Int
) {
    Slider(
        value         = qty.toFloat(),
        onValueChange = { onQtyChange(it.toInt()) },
        valueRange    = 0f..target.toFloat(),
        steps         = target - 1,

        /* 👇 Slot para el thumb (pomo) */
        thumb = {
            Icon(
                painter = painterResource(id = R.drawable.ic_tibio_tracker),
                contentDescription = null,
                tint = Color.Unspecified,      // deja el icono con sus propios colores
                modifier = Modifier.size(48.dp)
            )
        }
    )
}
