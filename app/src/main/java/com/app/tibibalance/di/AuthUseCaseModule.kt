package com.app.tibibalance.di

import com.app.domain.repository.AuthRepository
import com.app.domain.repository.EmotionRepository
import com.app.domain.repository.HabitRepository
import com.app.domain.repository.OnboardingRepository
import com.app.domain.repository.UserRepository
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SendResetPasswordUseCase
import com.app.domain.usecase.auth.SignInUseCase
import com.app.domain.usecase.auth.SignUpUseCase
import com.app.domain.usecase.auth.SyncAccount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// :app/di/AuthUseCaseModule.kt
@Module
@InstallIn(ViewModelComponent::class)
object AuthUseCaseModule {

    @Provides
    fun provideSyncAccount(
        habitRepo      : HabitRepository,
        emotionRepo    : EmotionRepository,
        onboardingRepo : OnboardingRepository,
        userRepo       : UserRepository,
        authRepo       : AuthRepository
    ) = SyncAccount(
        habitRepo,
        emotionRepo,
        onboardingRepo,
        userRepo,
        authRepo
    )

    @Provides
    fun provideSignInUseCase(
        repo: AuthRepository,
        syncAccount: SyncAccount
    ) = SignInUseCase(repo, syncAccount)

    @Provides
    fun provideSignUpUseCase(repo: AuthRepository) = SignUpUseCase(repo)

    @Provides
    fun provideForgotPasswordUseCase(repo: AuthRepository) = SendResetPasswordUseCase(repo)

    @Provides
    fun provideGoogleSignInUseCase(
        repo: AuthRepository,
        syncAccount: SyncAccount
    ) = GoogleSignInUseCase(repo, syncAccount)
}
