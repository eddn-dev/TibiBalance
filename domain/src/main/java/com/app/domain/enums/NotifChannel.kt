package com.app.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class NotifChannel(val id: String) {
    HABITS("habits"),
    EMOTIONS("emotions"),
    SYSTEM("system")
}
