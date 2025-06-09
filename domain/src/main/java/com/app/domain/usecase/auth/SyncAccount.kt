/* domain/usecase/auth/SyncAccount.kt */
package com.app.domain.usecase.auth

import com.app.domain.auth.AuthUidProvider
import com.app.domain.usecase.activity.SyncHabitActivities
import com.app.domain.usecase.achievement.SyncAchievements
import com.app.domain.usecase.emotions.SyncEmotions
import com.app.domain.usecase.habit.SyncHabits
import com.app.domain.usecase.onboarding.SyncOnboarding
import com.app.domain.usecase.user.SyncUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Sincroniza *toda* la cuenta offline-first.
 *
 * Cada capa concreta se delega a un caso de uso independiente.
 * No se inyectan repos directamente y se evita la dependencia con AuthRepository,
 * usando [AuthUidProvider] (uid o cadena vacía si no hay sesión).
 */
class SyncAccount @Inject constructor(
    private val uidProvider     : AuthUidProvider,
    private val syncHabits      : SyncHabits,
    private val syncEmotions    : SyncEmotions,
    private val syncOnboarding  : SyncOnboarding,
    private val syncUser        : SyncUser,
    private val syncActivities  : SyncHabitActivities,
    private val syncAchievements: SyncAchievements
) {

    suspend operator fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = uidProvider()
        if (uid.isBlank())
            return@withContext Result.failure(IllegalStateException("No user"))

        runCatching {
            /* 1️⃣  entidades locales (no requieren uid) */
            syncHabits().getOrThrow()
            syncEmotions().getOrThrow()
            syncActivities().getOrThrow()

            /* 2️⃣  entidades que viven bajo users/{uid}/… */
            syncOnboarding(uid).getOrThrow()
            syncUser(uid).getOrThrow()
            syncAchievements().getOrThrow()
        }
    }
}
