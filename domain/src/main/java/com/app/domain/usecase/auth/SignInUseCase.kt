// :domain / com/app/domain/usecase/auth/SignInUseCase.kt
package com.app.domain.usecase.auth

import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject   // <— sólo esta anotación

class SignInUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(credentials: UserCredentials): Result<Boolean> =
        withContext(Dispatchers.IO) {
            repo.signIn(credentials)
                .mapCatching {
                    repo.syncVerification().getOrNull()
                    repo.isEmailVerified()
                }
        }
}
