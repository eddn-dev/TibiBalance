package com.app.domain.usecase.auth

import com.app.domain.error.AuthResult
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val repo        : AuthRepository,
    private val syncAccount : SyncAccount          // ← ya incluye UserRepository
) {

    suspend operator fun invoke(idToken: String): AuthResult<Boolean> =
        withContext(Dispatchers.IO) {
            when (val r = repo.signInWithGoogle(idToken)) {
                /* 1️⃣  Auth OK   →  disparar sync (ignore errors) */
                is AuthResult.Success -> {
                    syncAccount()          // no modifica ‘r’; solo side-effect
                    r
                }
                /* 2️⃣  Auth Error →  propagar tal cual */
                is AuthResult.Error   -> r
            }
        }
}
