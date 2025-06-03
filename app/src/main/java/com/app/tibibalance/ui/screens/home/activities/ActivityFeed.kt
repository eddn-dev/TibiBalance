/**
 * @file    ActivityFeed.kt
 * @brief   Feed de actividades agrupado en: Próximamente ▸ Por registrar ▸
 *          Vencidas ▸ Completadas.
 */
package com.app.tibibalance.ui.screens.home.activities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.domain.entities.HabitActivity
import com.app.domain.enums.ActivityStatus
import com.app.tibibalance.ui.components.utils.SettingItem
import com.app.tibibalance.ui.theme.Green
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

/* ─────────────  Modelo auxiliar  ───────────── */

data class ActivityUi(
    val act  : HabitActivity,
    val name : String,
    val icon : ImageVector,
)

/* ─────────────  Feed principal  ───────────── */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityFeed(
    modifier   : Modifier = Modifier,
    activities : List<ActivityUi>,
    now        : Instant = Clock.System.now(),
    onClickCard: (ActivityUi) -> Unit
) {
    val grouped = activities.groupBy { it.bucket(now) }

    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped[Bucket.READY   ]?.let { section("Por registrar", it,  true,  onClickCard) }
        grouped[Bucket.UPCOMING]?.let { section("Próximamente", it, false, onClickCard) }
        grouped[Bucket.OVERDUE ]?.let { section("Vencidas",      it, false, onClickCard) }
        grouped[Bucket.DONE    ]?.let { section("Completadas",   it, false, onClickCard) }
    }
}

/* ─────────────  Sección con título  ───────────── */

@RequiresApi(Build.VERSION_CODES.O)
private fun LazyListScope.section(
    title     : String,
    items     : List<ActivityUi>,
    clickable : Boolean,
    onClick   : (ActivityUi) -> Unit
) {
    item {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
    items(items, key = { it.act.id.raw }) { ui ->
        ActivityCard(ui, clickable, onClick)
    }
}

/* ─────────────  Tarjeta individual  ───────────── */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ActivityCard(
    ui        : ActivityUi,
    clickable : Boolean,
    onClick   : (ActivityUi) -> Unit
) {
    /* fondo según estado */
    val bg = when (ui.act.status) {
        ActivityStatus.COMPLETED            -> Green
        ActivityStatus.PARTIALLY_COMPLETED  -> MaterialTheme.colorScheme.surfaceVariant
        ActivityStatus.MISSED               -> MaterialTheme.colorScheme.errorContainer
        else                                -> MaterialTheme.colorScheme.secondaryContainer
    }

    /* trailing (icono / progreso) */
    val trailing: @Composable () -> Unit = when (ui.act.status) {
        ActivityStatus.COMPLETED -> { {Icon(Icons.Default.CheckCircle, null) } }
        ActivityStatus.PARTIALLY_COMPLETED -> {
            {
                val r = ui.act.recordedQty ?: 0
                val t = ui.act.targetQty   ?: 0
                Text("$r/$t")
            }
        }
        ActivityStatus.MISSED -> { { Icon(Icons.Default.Pix, null) } }
        ActivityStatus.PENDING -> { { Icon(Icons.Default.Schedule, null) } }
        ActivityStatus.AVAILABLE_FOR_LOGGING -> { { Icon(Icons.AutoMirrored.Filled.Assignment, null) } }
    }

    /* label “Hoy / Mañana – HH:mm | Sin hora” */
    val tz        = TimeZone.currentSystemDefault()
    val todayDate = Clock.System.now().toLocalDateTime(tz).date
    val labelDay  = if (ui.act.activityDate == todayDate) "Hoy" else "Mañana"
    val labelTime = ui.act.scheduledTime?.timeFmt() ?: "Cualquier momento"
    val desc      = "${ui.name}\n$labelDay, $labelTime"

    SettingItem(
        leadingIcon = {
            Icon(ui.icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
        },
        text          = desc,
        trailing      = trailing,
        onClick       = if (clickable) { { onClick(ui) } } else null,
        containerColor= bg,
        cornerRadius  = 12.dp
    )
}

/* ─────────────  Clasificación (bucket)  ───────────── */

private enum class Bucket { UPCOMING, READY, OVERDUE, DONE }

private fun ActivityUi.bucket(now: Instant): Bucket = when (act.status) {
    ActivityStatus.COMPLETED,
    ActivityStatus.PARTIALLY_COMPLETED        -> Bucket.DONE

    ActivityStatus.PENDING,
    ActivityStatus.AVAILABLE_FOR_LOGGING      -> {
        val open  = act.opensAt   ?: Instant.DISTANT_PAST
        val close = act.expiresAt ?: Instant.DISTANT_FUTURE
        when {
            now <  open -> Bucket.UPCOMING
            now >  close -> Bucket.OVERDUE
            else -> Bucket.READY
        }
    }

    ActivityStatus.MISSED                    -> Bucket.OVERDUE
}

/* ─────────────  Formato HH:mm  ───────────── */

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalTime.timeFmt(): String =
    DateTimeFormatter.ofPattern("HH:mm")
        .format(java.time.LocalTime.of(hour, minute))
