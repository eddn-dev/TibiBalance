package com.app.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType {
    CREATED, EDITED, DELETED, ALARM, SNOOZED, COMPLETE, RESET, SKIPPED
}
