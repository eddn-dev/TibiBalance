package com.app.domain.usecase.user

import com.app.domain.repository.UserRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class UpdateUserProfile @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(
        uid: String,
        name: String? = null,
        dob : LocalDate? = null,
        photoUrl: String? = null
    ): Result<Unit> = repo.updateProfile(uid, name, dob, photoUrl)
}