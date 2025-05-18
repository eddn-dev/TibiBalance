// :domain/usecase/auth/SignUpUseCase.kt
package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(c: UserCredentials, name: String, dob: LocalDate): AuthResult<Unit> =
        withContext(Dispatchers.IO) {
            repo.signUp(c, name, dob)       // ⇒ envía correo de verificación y crea user doc
        }
}
