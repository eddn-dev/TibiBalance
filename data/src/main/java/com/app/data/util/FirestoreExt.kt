package com.app.data.util

// util/FirestoreExt.kt
import com.google.firebase.Timestamp
import kotlinx.datetime.Instant
import java.util.Date

/** Convierte un Instant en Timestamp solo si está en el rango válido. */
fun Instant?.safeTimestamp(): Timestamp? =
    this
        ?.takeUnless { it == Instant.DISTANT_PAST }
        ?.let { Timestamp(Date(it.toEpochMilliseconds())) }
