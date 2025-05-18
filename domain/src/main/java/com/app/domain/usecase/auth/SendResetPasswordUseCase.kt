package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendResetPasswordUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> =
        withContext(Dispatchers.IO) { repo.sendPasswordReset(email) }
}
