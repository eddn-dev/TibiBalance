/* :domain/usecase/dailytips/MarkTipShownUseCase.kt */
package com.app.domain.usecase.dailytips

import com.app.domain.repository.DailyTipsRepository
import javax.inject.Inject

class MarkTipShownUseCase @Inject constructor(
    private val repo: DailyTipsRepository
) {
    suspend operator fun invoke(id: Int) = repo.markAsShown(id)
}
