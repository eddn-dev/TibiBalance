/* :app/ui/components/DailyTip.kt */
package com.app.tibibalance.ui.components.containers

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.domain.entities.Challenge
import com.app.domain.entities.DailyTip
import com.app.domain.entities.DailyTipItem
import com.app.domain.entities.Item
import com.app.domain.entities.Link
import com.app.domain.entities.Text
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.texts.Subtitle
import com.app.tibibalance.ui.components.texts.Title
import com.app.tibibalance.ui.theme.LinkText
import com.app.tibibalance.ui.theme.DailyTip as DailyTipColor
import androidx.core.net.toUri

/** Tarjeta que muestra el “Tip del día” completo. */
@Composable
fun DailyTip(
    tip: DailyTip,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(24.dp)) {

        /* Encabezado global */
        Title("Tip del día")

        Spacer(Modifier.height(16.dp))

        /* Card principal */
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(containerColor = DailyTipColor),
            shape    = CardDefaults.shape
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                /* Columna de texto ocupa todo el espacio restante */
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),          // separa del icono
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Subtitle(tip.title)
                    Text(tip.subtitle, style = MaterialTheme.typography.bodyMedium)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tip.content) { item -> TipRow(item) }
                    }
                }

                /* Icono principal (72 dp), alineado arriba-derecha */
                val tipIcon = iconForName(tip.icon)
                Image(
                    painter = painterResource(tipIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Top)
                )
            }
        }
    }
}

/* Ítems individuales ---------------------------------------------------------------- */
@Composable
private fun TipRow(item2: DailyTipItem) {
    var item = item2
    if(item is Text)
        item = Challenge(item.text)
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
            is Text -> Spacer(Modifier.width(32.dp))
            is Link ->  Spacer(Modifier.width(32.dp))
            is Challenge -> Spacer(Modifier.width(32.dp))
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
        style = MaterialTheme.typography.bodyMedium.copy(color = LinkText),
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
    "walking"            -> R.drawable.ic_happyimage
    "stretching"         -> R.drawable.ic_happyimage
    "energy_bolt"        -> R.drawable.ic_happyimage
    "ic_tibio_sleepy"    -> R.drawable.ic_happyimage
    "ic_tibio_healthy"   -> R.drawable.ic_happyimage
    "ic_tibio_smart"     -> R.drawable.ic_happyimage
    "ic_tibio_calm"      -> R.drawable.ic_happyimage
    "ic_tibio_thoughtfull"-> R.drawable.ic_happyimage
    "ic_tibio_kind"      -> R.drawable.ic_happyimage
    "ic_tibio_energetic" -> R.drawable.ic_happyimage
    "ic_tibio_happy"     -> R.drawable.ic_happyimage
    else                 -> R.drawable.ic_happyimage
}
