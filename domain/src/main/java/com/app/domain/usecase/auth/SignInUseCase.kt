package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(c: UserCredentials): AuthResult<Boolean> =
        withContext(Dispatchers.IO) {
            when (val r = repo.signIn(c)) {
                is AuthResult.Success -> {
                    // intenta sincronizar, pero no sobre-escribe el resultado de signIn
                    repo.syncVerification()
                    r                    // mismo Success con verificado = r.data
                }
                is AuthResult.Error   -> r
            }
        }
}
