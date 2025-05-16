package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * @file    UserDao.kt
 * @ingroup data_local_dao
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM users LIMIT 1")
    fun observeUser(): Flow<UserEntity?>

    @Upsert
    suspend fun upsert(user: UserEntity)

    /** Sólo actualiza ajustes sin sobreescribir perfil. */
    @Query("""
        UPDATE users
        SET settings_theme = :theme,
            settings_notif = :notif,
            settings_lang  = :lang,
            settings_tts   = :tts,
            meta_updatedAt = :updatedAt,
            meta_pendingSync = 1
        WHERE uid = :uid
    """)
    suspend fun updateSettings(
        uid: String,
        theme: String,
        notif: Boolean,
        lang: String,
        tts: Boolean,
        updatedAt: Long
    )
}
