// app/src/main/java/com/app/tibibalance/di/WearModule.kt
package com.app.tibibalance.di

import android.content.Context
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearModule {

    @Provides
    @Singleton
    fun provideNodeClient(
        @ApplicationContext context: Context
    ): NodeClient = Wearable.getNodeClient(context)
}
