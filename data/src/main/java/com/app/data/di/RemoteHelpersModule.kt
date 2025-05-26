package com.app.data.di

import android.content.Context
import com.app.data.remote.PhotoUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteHelpersModule {
    @Provides
    @Singleton
    fun providePhotoUploader(
        @ApplicationContext ctx: Context
    ) = PhotoUploader(ctx)
}
