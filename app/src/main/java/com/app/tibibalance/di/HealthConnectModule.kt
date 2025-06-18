/* di/HealthConnectModule.kt */
package com.app.tibibalance.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.app.tibibalance.utils.HealthConnectAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule {

    @Provides @Singleton
    fun provideHealthConnectAvailability(
        @ApplicationContext context: Context
    ): HealthConnectAvailability = HealthConnectAvailability(context)

    /** Devuelve null cuando HC no está listo. */
    @Provides
    fun provideHealthConnectClient(
        @ApplicationContext context: Context,
        availability: HealthConnectAvailability
    ): HealthConnectClient? =
        if (availability.isHealthConnectReady())
            HealthConnectClient.getOrCreate(context)
        else
            null
}
