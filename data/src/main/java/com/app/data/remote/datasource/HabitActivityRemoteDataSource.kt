/**
 * @file    HabitActivityRemoteDataSource.kt
 * @ingroup data_remote_datasource
 */
package com.app.data.remote.datasource

import com.app.domain.entities.HabitActivity
import com.app.domain.ids.ActivityId

/**
 * Operaciones CRUD / sync contra la colección remota
 * `users/{uid}/activities`.
 */
interface HabitActivityRemoteDataSource {

    /** Sube (crea o reemplaza) una actividad. */
    suspend fun pushActivity(uid: String, activity: HabitActivity)

    /** Elimina permanentemente una actividad remota. */
    suspend fun deleteActivity(uid: String, id: ActivityId)

    /** Descarga todas las actividades del usuario. */
    suspend fun fetchUserActivities(uid: String): List<HabitActivity>
}
