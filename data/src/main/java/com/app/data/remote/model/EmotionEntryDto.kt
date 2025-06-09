package com.app.data.remote.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EmotionEntryDto(
    val date : String,          // ISO-8601
    val mood : String,          // = Emotion.name
    val updatedAt: Instant
)
