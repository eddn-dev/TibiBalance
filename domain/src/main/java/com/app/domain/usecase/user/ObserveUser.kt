package com.app.domain.usecase.user

import com.app.domain.entities.User
import com.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUser @Inject constructor(
    private val repo: UserRepository
) {
    operator fun invoke(uid: String): Flow<User> = repo.observe(uid)
}
