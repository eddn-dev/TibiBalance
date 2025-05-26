package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(
        current: String,
        newer  : String
    ): AuthResult<Unit> =
        repo.changePassword(current, newer)
}
