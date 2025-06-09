// :domain/usecase/user/GetUserSettings.kt
package com.app.domain.usecase.user

import com.app.domain.auth.AuthUidProvider      // helper que ya existe
import com.app.domain.entities.UserSettings
import com.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Devuelve en tiempo real las preferencias del usuario autenticado.
 * Lanza IllegalStateException si no hay sesión.
 */
class GetUserSettings @Inject constructor(
    private val repo : UserRepository,
    private val uid  : AuthUidProvider          // () -> String?
) {

    operator fun invoke(): Flow<UserSettings> {
        val current = uid() ?: error("No uid")
        return repo.observe(current)            // Flow<User>
            .map { it.settings }                // Flow<UserSettings>
    }
}
