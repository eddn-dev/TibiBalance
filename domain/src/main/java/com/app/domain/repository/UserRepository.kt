/* :domain/repository/UserRepository.kt */
package com.app.domain.repository

import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface UserRepository {

    fun observe(uid: String): Flow<User>

    suspend fun updateProfile(
        uid        : String,
        displayName: String?    = null,
        birthDate  : LocalDate? = null,
        photoUrl   : String?    = null
    ): Result<Unit>

    suspend fun updateSettings(uid: String, settings: UserSettings): Result<Unit>

    suspend fun syncNow(uid: String): Result<Unit>
}
