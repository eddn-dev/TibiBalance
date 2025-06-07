/* :app/ui/components/DailyTip.kt */
package com.app.tibibalance.ui.components.containers

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.domain.entities.Challenge
import com.app.domain.entities.DailyTip
import com.app.domain.entities.DailyTipItem
import com.app.domain.entities.Item
import com.app.domain.entities.Link
import com.app.domain.entities.Text
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.texts.Title
import androidx.core.net.toUri
import com.app.tibibalance.ui.components.texts.Description
import androidx.compose.ui.text.TextStyle

/** Tarjeta que muestra el “Tip del día” completo. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DailyTip(
    tip: DailyTip
) {
    Column{

        /* Encabezado global */
        Title("Tip del día")

        Spacer(Modifier.height(16.dp))

        /* Card principal */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_tip_card"),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape    = CardDefaults.shape
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                /* Columna de texto ocupa todo el espacio restante */
                Row (
                    modifier = Modifier

                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 12.dp),          // separa del icono
                    horizontalArrangement = Arrangement.spacedBy(12.dp)

                ) {
                    Column(
                        // ocupa 2 fracciones de 3
                        modifier = Modifier.weight(7f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)

                    ) {
                       Subtitle(tip.title)
                       Description(tip.subtitle, style = TextStyle(
                           fontFamily = FontFamily.Default,
                           fontWeight = FontWeight.Normal,
                           fontSize = 18.sp,
                           lineHeight = 18.sp))
                    }

                    val tipIcon = iconForName(tip.icon)
                    Image(
                        painter = painterResource(tipIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(3f)
                            .align(Alignment.Top)
                    )

                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tip.content) { item -> TipRow(item) }
                }
            }
        }
    }
}

/* Ítems individuales ---------------------------------------------------------------- */
@Composable
private fun TipRow(item: DailyTipItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        when (item) {
            is Item -> {                 // ítem con icono
                Image(
                    painter = painterResource(iconForName(item.icon)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
            }
            is Text -> Spacer(Modifier.width(12.dp))
            is Link ->  Spacer(Modifier.width(12.dp))
            is Challenge -> Spacer(Modifier.width(12.dp))
        }

        when (item) {
            is Link -> ClickableLink(item)
            is Challenge -> ChallengeText(item)
            else -> Text(item.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ClickableLink(link: Link) {
    val context = LocalContext.current

    Text(
        text  = link.text,
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, link.target.toUri())
            context.startActivity(intent)
        }
    )
}

@Composable
private fun ChallengeText(ch: Challenge) {
    Text(
        text = ch.text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .background(
                color  = MaterialTheme.colorScheme.secondary,
                shape  = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}


/* Utilidad para mapear nombres de Firestore a drawables locales ---------------------- */
@DrawableRes
private fun iconForName(name: String): Int = when (name) {
    "ic_tibio_happy"            -> R.drawable.ic_tibio_happy
    "ic_tibio_energetic"        -> R.drawable .ic_tibio_energetic
    "ic_tibio_curious"          -> R.drawable.ic_tibio_curious
    "ic_tibio_relaxed"          -> R.drawable.ic_tibio_relaxed
    "ic_tibio_thirsty"          -> R.drawable.ic_tibio_thirsty
    "ic_tibio_joyful"           -> R.drawable.ic_tibio_joyful
    "ic_tibio_grateful"         -> R.drawable.ic_tibio_grateful
    "ic_tibio_connected"        -> R.drawable.ic_tibio_connected
    "ic_tibio_sleepy"           -> R.drawable.ic_tibio_sleepy
    "ic_tibio_healthy"          -> R.drawable.ic_tibio_healthy
    "ic_tibio_smart"            -> R.drawable.ic_tibio_smart
    "ic_tibio_calm"             -> R.drawable.ic_tibio_calm
    "ic_tibio_thoughtful"       -> R.drawable.ic_tibio_thoughtful
    "ic_tibio_kind"             -> R.drawable.ic_tibio_kind
    else                        -> R.drawable.ic_happyimage
}
