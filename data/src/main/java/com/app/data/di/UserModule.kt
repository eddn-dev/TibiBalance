package com.app.data.di

import com.app.data.local.dao.UserDao
import com.app.data.repository.IoDispatcher
import com.app.data.repository.UserRepositoryImpl
import com.app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        db : FirebaseFirestore,
        dao: UserDao,
        @IoDispatcher io: CoroutineDispatcher
    ): UserRepository = UserRepositoryImpl(db, dao, io)
}
