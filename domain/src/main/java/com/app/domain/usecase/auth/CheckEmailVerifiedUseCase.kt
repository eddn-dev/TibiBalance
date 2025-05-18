package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import javax.inject.Inject

class CheckEmailVerifiedUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(): AuthResult<Boolean> {
        when (val r = repo.reload()) {
            is AuthResult.Error -> return r          // error de red, etc.
            else -> Unit
        }
        val verified = repo.isEmailVerified()
        if (verified) repo.syncVerification()
        return AuthResult.Success(verified)
    }
}