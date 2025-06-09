/* :app/di/AuthUseCaseModule.kt */
package com.app.tibibalance.di

import com.app.domain.auth.AuthUidProvider
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.achievement.SyncAchievements
import com.app.domain.usecase.activity.SyncHabitActivities
import com.app.domain.usecase.auth.*
import com.app.domain.usecase.emotions.SyncEmotions
import com.app.domain.usecase.habit.SyncHabits
import com.app.domain.usecase.onboarding.SyncOnboarding
import com.app.domain.usecase.user.SyncUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object AuthUseCaseModule {

    /* ───────── 1. SyncAccount  ───────── */

    @Provides
    fun provideSyncAccount(
        uidProvider     : AuthUidProvider,
        syncHabits      : SyncHabits,
        syncEmotions    : SyncEmotions,
        syncOnboarding  : SyncOnboarding,
        syncUser        : SyncUser,
        syncActivities  : SyncHabitActivities,
        syncAchievements: SyncAchievements
    ): SyncAccount = SyncAccount(
        uidProvider,
        syncHabits,
        syncEmotions,
        syncOnboarding,
        syncUser,
        syncActivities,
        syncAchievements
    )

    /* ───────── 2. Auth flows ───────── */

    @Provides
    fun provideSignInUseCase(
        repo        : AuthRepository,
        syncAccount : SyncAccount
    ) = SignInUseCase(repo, syncAccount)

    @Provides
    fun provideSignUpUseCase(repo: AuthRepository) =
        SignUpUseCase(repo)

    @Provides
    fun provideForgotPasswordUseCase(repo: AuthRepository) =
        SendResetPasswordUseCase(repo)

    @Provides
    fun provideGoogleSignInUseCase(
        repo        : AuthRepository,
        syncAccount : SyncAccount
    ) = GoogleSignInUseCase(repo, syncAccount)
}
