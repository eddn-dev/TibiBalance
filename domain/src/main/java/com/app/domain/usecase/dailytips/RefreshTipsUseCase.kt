/* :domain/usecase/dailytips/RefreshTipsUseCase.kt */
package com.app.domain.usecase.dailytips

import com.app.domain.repository.DailyTipsRepository
import javax.inject.Inject

class RefreshTipsUseCase @Inject constructor(
    private val repo: DailyTipsRepository
) {
    suspend operator fun invoke() = repo.refresh()
}
