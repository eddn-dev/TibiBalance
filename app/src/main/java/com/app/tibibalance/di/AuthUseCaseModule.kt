package com.app.tibibalance.di

import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.GoogleSignInUseCase
import com.app.domain.usecase.auth.SendResetPasswordUseCase
import com.app.domain.usecase.auth.SignInUseCase
import com.app.domain.usecase.auth.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// :app/di/AuthUseCaseModule.kt
@Module
@InstallIn(ViewModelComponent::class)
object AuthUseCaseModule {

    @Provides
    fun provideSignInUseCase(repo: AuthRepository) = SignInUseCase(repo)

    @Provides
    fun provideSignUpUseCase(repo: AuthRepository) = SignUpUseCase(repo)

    @Provides
    fun provideForgotPasswordUseCase(repo: AuthRepository) = SendResetPasswordUseCase(repo)

    @Provides
    fun provideGoogleSignInUseCase(repo: AuthRepository) = GoogleSignInUseCase(repo)
}
