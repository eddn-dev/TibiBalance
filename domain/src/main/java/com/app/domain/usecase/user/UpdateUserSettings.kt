package com.app.domain.usecase.user

import com.app.domain.entities.UserSettings
import com.app.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserSettings @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(uid: String, settings: UserSettings): Result<Unit> =
        repo.updateSettings(uid, settings)
}
