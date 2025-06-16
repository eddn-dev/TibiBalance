/* domain/usecase/user/SyncUser.kt */
package com.app.domain.usecase.user

import com.app.domain.repository.UserRepository
import javax.inject.Inject

class SyncUser @Inject constructor(
    private val repo: UserRepository
) {
    /** Perfil de usuario también requiere uid. */
    suspend operator fun invoke(uid: String): Result<Unit> = repo.syncNow(uid)
}
