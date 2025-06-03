// domain/usecase/auth/DeleteAccountUseCase.kt
package com.app.domain.usecase.auth

import com.app.domain.repository.AuthRepository
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.deleteAccount()
    }
}