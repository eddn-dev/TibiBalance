/* :domain/usecase/auth/ClearLocalData.kt */
package com.app.domain.usecase.auth

import com.app.domain.repository.LocalDataRepository
import javax.inject.Inject

class ClearLocalData @Inject constructor(
    private val localRepo: LocalDataRepository
) {
    suspend operator fun invoke() = localRepo.clearAll()
}
