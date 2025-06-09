/**
 * @file    AchievementRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Operaciones CRUD aisladas de Firestore para los logros.
 *
 *  Ruta en la nube:
 *      users/{uid}/achievements/{achievementId}
 */
package com.app.data.remote.datasource

import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId

interface AchievementRemoteDataSource {

    /** Descarga todos los logros del usuario (usuarios nuevos pueden recibir lista vacía). */
    suspend fun fetchAll(uid: String): List<Achievement>

    /** Sube o fusiona un logro (usa `SetOptions.merge()` → idempotente). */
    suspend fun push(uid: String, achievement: Achievement)

    /** Borra el documento remoto (rara vez necesitarás esto, pero por simetría). */
    suspend fun delete(uid: String, id: AchievementId)
}
