/* :domain/repository/LocalDataRepository.kt */
package com.app.domain.repository

/**  Acceso a los datos *offline first* que pueden borrarse al logout. */
interface LocalDataRepository {
    /** Borra tablas locales (hábitos, emociones, ajustes, etc.). */
    suspend fun clearAll()
}
