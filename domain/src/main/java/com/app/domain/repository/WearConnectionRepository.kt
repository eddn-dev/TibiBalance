/* :domain/repository/WearConnectionRepository.kt */
package com.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface WearConnectionRepository {
    /** `true` ⇢ hay al menos un reloj Wear OS emparejado y accesible. */
    fun isWatchConnected(): Flow<Boolean>
}
