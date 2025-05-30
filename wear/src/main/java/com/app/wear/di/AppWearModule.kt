package com.app.wear.di

import android.content.Context
import com.app.wear.data.datasource.ICommunicationDataSource
import com.app.wear.data.datasource.ISensorDataSource
import com.app.wear.data.datasource.MockSensorDataSource
import com.app.wear.data.datasource.WearableApiCommDataSource
import com.app.wear.data.repository.WearMetricsRepositoryImpl
import com.app.wear.domain.repository.IWearMetricsRepository
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppWearModule {

    @Provides
    @Singleton
    fun provideDataClient(@ApplicationContext context: Context): DataClient =
        Wearable.getDataClient(context)

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideCommunicationDataSource(
        dataClient: DataClient,
        json: Json
    ): ICommunicationDataSource = WearableApiCommDataSource(dataClient, json)

    @Provides
    @Singleton
    fun provideSensorDataSource(): ISensorDataSource = MockSensorDataSource()

    @Provides
    @Singleton
    fun provideMetricsRepository(
        sensorDataSource: ISensorDataSource,
        communicationDataSource: ICommunicationDataSource
    ): IWearMetricsRepository =
        WearMetricsRepositoryImpl(sensorDataSource, communicationDataSource)
}
