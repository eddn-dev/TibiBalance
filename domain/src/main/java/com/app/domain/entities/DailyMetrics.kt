package com.app.domain.entities

import com.app.domain.common.SyncMeta
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * @file    DailyMetrics.kt
 * @ingroup domain_entities
 * @brief   Métricas biométricas importadas (`metrics/{date}`).
 */
@Serializable
data class DailyMetrics(
    val date       : LocalDate, // PK
    val steps      : Int,
    val avgHeart   : Int?       = null,
    val calories   : Int?       = null,
    val source     : String,
    val importedAt : Instant    = Instant.DISTANT_PAST,
    val meta       : SyncMeta   = SyncMeta()
)
