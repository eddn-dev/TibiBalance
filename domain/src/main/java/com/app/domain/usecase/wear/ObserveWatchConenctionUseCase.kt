/* :domain/usecase/wear/ObserveWatchConnectionUseCase.kt */
package com.app.domain.usecase.wear

import com.app.domain.repository.WearConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWatchConnectionUseCase @Inject constructor(
    private val repo: WearConnectionRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.isWatchConnected()
}
