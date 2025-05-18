/* data/di/SerializationModule.kt */
package com.app.data.di

import com.app.data.mappers.JsonConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    /** Instancia única y configurada de kotlinx-serialization que usarán todos los mappers. */
    @Provides
    @Singleton
    fun provideJson(): Json = JsonConfig.default
}
