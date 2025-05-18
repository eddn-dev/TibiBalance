package com.app.data.remote.model

import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import java.util.Date

/* --- Instant ⇄ Timestamp --------------------------------------------- */
internal fun Instant?.toTimestamp(): Timestamp? =
    this?.let { Timestamp(Date(it.toEpochMilliseconds())) }

internal fun Timestamp?.toInstant(): Instant? =
    this?.toDate()?.toInstant()        // java.util.Date → java.time.Instant
        ?.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) }
