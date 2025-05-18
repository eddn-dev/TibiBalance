package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AuthResult<Boolean> =
        withContext(Dispatchers.IO) { repo.signInWithGoogle(idToken) }
}
