package com.app.tibibalance.di    // si lo dejas en :app

import com.app.data.repository.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @IoDispatcher
    @Provides @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
