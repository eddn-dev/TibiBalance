/*
 * @file    ActivityFeed.kt
 * @ingroup ui_component_feed
 * @brief   Lista dinámica de tarjetas de actividades de hábitos-reto.
 *
 *  • Agrupa automáticamente en:
 *      1) Por registrar
 *      2) Próximamente
 *      3) Completadas
 *  • Cada tarjeta usa SettingItem para aspecto consistente.
 *  • Colores se toman de MaterialTheme y cambian con light/dark.
 */

package com.app.tibibalance.ui.components.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.domain.entities.HabitActivity
import com.app.domain.enums.ActivityStatus
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.components.utils.SettingItem
import java.time.format.DateTimeFormatter
import kotlinx.datetime.*

/* ──────────────── MODELO AUXILIAR ──────────────── */

/**
 * Agrupa la actividad con la información de su hábito.
 */
data class ActivityUi(
    val act: HabitActivity,
    val name: String,      // habit.name
    val icon: ImageVector        // drawable id del icono del hábito
)

/* ──────────────── COMPOSABLE PRINCIPAL ──────────────── */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityFeed(
    modifier   : Modifier = Modifier,
    activities : List<ActivityUi>,
    now        : LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    ),
    onClickCard: (ActivityUi) -> Unit,

) {
    /* 1. Clasificar */
    val (ready, upcomingTmp) = activities.partition { it.isReady(now) }
    val (upcoming, done)     = upcomingTmp.partition { it.act.status == ActivityStatus.PENDING }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (ready.isNotEmpty())   section("Por registrar", ready, onClickCard)
        if (upcoming.isNotEmpty()) section("Próximamente", upcoming, onClickCard)
        if (done.isNotEmpty())    section("Completadas", done,  onClickCard)
    }
}

/* ──────────────── HELPERS UI ──────────────── */

@RequiresApi(Build.VERSION_CODES.O)
private fun LazyListScope.section(
    title : String,
    items : List<ActivityUi>,
    onClick: (ActivityUi) -> Unit
) {
    item {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
    items(
        items = items,
        key   = { it.act.id.raw }
    ) { ui ->
        ActivityCard(ui, onClick)
    }
}

/* Tarjeta individual (envuelta en SettingItem) */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ActivityCard(
    ui      : ActivityUi,
    onClick : (ActivityUi) -> Unit
) {


    /* Colores según estado */
    val colors = MaterialTheme.colorScheme
    val bg = when (ui.act.status) {
        ActivityStatus.PENDING,
        ActivityStatus.AVAILABLE_FOR_LOGGING -> colors.surfaceVariant
        ActivityStatus.COMPLETED            -> colors.tertiaryContainer
        ActivityStatus.PARTIALLY_COMPLETED  -> colors.secondaryContainer
        ActivityStatus.MISSED               -> colors.errorContainer
    }

    /* Trailing – icono o progreso */
    val trailing: @Composable () -> Unit = when (ui.act.status) {
        ActivityStatus.PENDING,
        ActivityStatus.AVAILABLE_FOR_LOGGING -> {
            {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Programado"
                )
            }
        }
        ActivityStatus.COMPLETED -> {
            { Icon(Icons.Default.CheckCircle, contentDescription = "Completado") }
        }
        ActivityStatus.PARTIALLY_COMPLETED -> {
            {
                val target = ui.act.targetQty ?: 0
                val rec    = ui.act.recordedQty ?: 0
                Text("$rec/$target")
            }
        }
        ActivityStatus.MISSED -> { { Text("—") } }
    }

    /* Texto hora / “Cualquier momento” */
    val desc = buildString {
        append(ui.name)
        val lt = ui.act.scheduledTime
        append(" • ")
        append(
            lt?.let { DateTimeFormatter.ofPattern("HH:mm").format(it.toJavaLocalTime()) }
                ?: "Cualquier momento"
        )
    }

    SettingItem(
        leadingIcon = {
            Icon(
                imageVector = ui.icon,          // ⬅️ usa el vector directamente
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        text          = desc,
        trailing      = trailing,
        onClick       = { onClick(ui) },
        containerColor= bg,
        cornerRadius  = 12.dp
    )
}

/* ──────────────── LÓGICA DE ESTADO ──────────────── */

private fun ActivityUi.isReady(now: LocalDateTime): Boolean {
    val statusReady = when (act.status) {
        ActivityStatus.PENDING                -> false    // aún no
        ActivityStatus.AVAILABLE_FOR_LOGGING  -> true
        ActivityStatus.COMPLETED,
        ActivityStatus.PARTIALLY_COMPLETED,
        ActivityStatus.MISSED                 -> false
    }
    val timeReady  = act.scheduledTime?.let { lt ->
        now.hour == lt.hour && now.minute >= lt.minute
    } ?: true
    return statusReady || timeReady
}

/* util rápido */
@RequiresApi(Build.VERSION_CODES.O)
private fun LocalTime.toJavaLocalTime(): java.time.LocalTime =
    java.time.LocalTime.of(hour, minute)
