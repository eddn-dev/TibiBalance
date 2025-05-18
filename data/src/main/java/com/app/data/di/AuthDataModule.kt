/**
 * @file      AuthDataModule.kt
 * @ingroup   data_di
 * @brief     Binds AuthRepository to its concrete implementation for Hilt.
 */
package com.app.data.di

import com.app.data.repository.AuthRepositoryImpl
import com.app.domain.repository.AuthRepository
import com.app.domain.usecase.auth.SignInUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
