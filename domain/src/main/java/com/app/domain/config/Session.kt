// com/app/domain/config/Session.kt
package com.app.domain.config

import com.app.domain.enums.SessionUnit
import kotlinx.serialization.Serializable

@Serializable
data class Session(val qty: Int? = null, val unit: SessionUnit = SessionUnit.INDEFINIDO)

