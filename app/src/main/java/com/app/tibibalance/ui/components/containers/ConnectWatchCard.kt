

/* :app/ui/components/containers/ConnectWatchCard.kt */
package com.app.tibibalance.ui.components.containers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.app.tibibalance.R
import com.app.tibibalance.ui.theme.Tips            // color Tips
import com.app.tibibalance.ui.theme.LinkText       // color LinkText

@Composable
fun ConnectWatchCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Tips, shape = MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_fruit_watch),
                contentDescription = "Personaje con reloj",
                modifier = Modifier
                    .size(80.dp)
                    .padding(8.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "¡Mejora tu monitoreo!\nConecta tu reloj.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "(Toca para conectar)",
                    style = MaterialTheme.typography.labelLarge.copy(color = LinkText)
                )
            }
        }
    }
}
