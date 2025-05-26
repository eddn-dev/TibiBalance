package com.app.domain.usecase.auth

import com.app.domain.repository.AuthRepository
import javax.inject.Inject

// SignOutUseCase

class SignOutUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke() = repo.signOut()
}