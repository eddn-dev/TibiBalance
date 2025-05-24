/**
 * @file    HabitRemoteDataSource.kt
 * @ingroup data_remote_datasource
 * @brief   Contrato CRUD para Firestore aislado de Room y del repo.
 */
package com.app.data.remote.datasource

import com.app.domain.entities.Habit
import com.app.domain.entities.HabitActivity
import com.app.domain.ids.HabitId

/**
 * @file    HabitRemoteDataSource.kt
 * @brief   Operaciones remotas contra Firestore.
 *
 * Colecciones:
 *   • users/{uid}/habits              (hábitos del usuario)
 *   • habitTemplates                  (plantillas sugeridas)
 *   • users/{uid}/habitActivities     (tracking de actividades)
 */
/**
 * @file    HabitRemoteDataSource.kt
 * @brief   Operaciones remotas contra Firestore.
 */

interface HabitRemoteDataSource {

    /* ─────────── Plantillas ─────────── */
    suspend fun fetchTemplates()            : List<Habit>   // habitTemplates (read-only)

    /* ─────────── Hábitos de usuario ─── */
    suspend fun fetchUserHabits(uid: String): List<Habit>   // users/{uid}/habits
    suspend fun pushHabit(uid: String, habit: Habit)
    suspend fun deleteHabit(uid: String, id: HabitId)

    /* ─────────── Actividades ────────── */
    suspend fun pushActivity(uid: String, activity: HabitActivity)
}

