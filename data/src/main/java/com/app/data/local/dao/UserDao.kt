/* data/local/dao/UserDao.kt */
package com.app.data.local.dao

import androidx.room.*
import com.app.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /* ---- lectura reactiva ---- */

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observe(uid: String): Flow<UserEntity?>

    /* ---- lectura puntual ---- */

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun find(uid: String): UserEntity?

    /* ---- inserción / reemplazo ---- */

    @Upsert
    suspend fun upsert(user: UserEntity)

    /* ---- settings “solo escritura” ---- */
    @Query(
        """
        UPDATE users
        SET settings_theme = :theme,
            settings_notif = :notif,
            settings_emotion = :notifE,
            settings_lang  = :lang,
            settings_tts   = :tts,
            meta_updatedAt = :updatedAt,
            meta_pendingSync = 1
        WHERE uid = :uid
        """
    )
    suspend fun updateSettings(
        uid: String,
        theme: String,
        notif: Boolean,
        notifE : Boolean,
        lang: String,
        tts: Boolean,
        updatedAt: Long
    )

    /* ---- utilidades de sincronización ---- */

    /** Regresa los registros con `pendingSync = 1` para el usuario. */
    @Query("SELECT * FROM users WHERE meta_pendingSync = 1 AND uid = :uid")
    suspend fun pendingToSync(uid: String): List<UserEntity>

    /** Marca un perfil como ya sincronizado. */
    @Query(
        """
        UPDATE users
        SET meta_pendingSync = 0
        WHERE uid = :uid
        """
    )
    suspend fun clearPending(uid: String)

    //Borra todos los registros de la tabla
    @Query("DELETE FROM users")
    suspend fun clear()
}
