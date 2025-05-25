package com.app.domain.usecase.user

import com.app.domain.repository.UserRepository
import javax.inject.Inject

class SyncUserNow @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(uid: String) = repo.syncNow(uid)
}