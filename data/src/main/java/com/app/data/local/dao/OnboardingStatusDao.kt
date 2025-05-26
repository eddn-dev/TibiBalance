package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.OnboardingStatusEntity
import kotlinx.coroutines.flow.Flow

/**
 * @file    OnboardingStatusDao.kt
 * @ingroup data_local_dao
 */
@Dao
interface OnboardingStatusDao {

    @Query("SELECT * FROM onboarding_status WHERE uid = :uid LIMIT 1")
    fun observe(uid: String): Flow<OnboardingStatusEntity?>

    @Upsert
    suspend fun upsert(status: OnboardingStatusEntity)

    @Query("SELECT * FROM onboarding_status WHERE uid = :uid LIMIT 1")
    suspend fun find(uid: String): OnboardingStatusEntity?

    //Borra todos los registros de la tabla
    @Query("DELETE FROM onboarding_status")
    suspend fun clear()
}
