/* :domain/usecase/dailytips/GetTodayTipUseCase.kt */
package com.app.domain.usecase.dailytips

import com.app.domain.entities.DailyTip
import com.app.domain.repository.DailyTipsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayTipUseCase @Inject constructor(
    private val repo: DailyTipsRepository
) {
    operator fun invoke(): Flow<DailyTip?> = repo.todayTip()
}
