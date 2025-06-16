/**
 * @file ProviderModule.kt
 * @brief Registro Hilt de providers, clientes Health* y canal Data Layer.
 */
package com.app.wear.di

import android.content.Context
import com.app.wear.data.datasource.WearableApiCommDataSource
import com.app.wear.data.provider.hc.HcHeartRateProvider
import com.app.wear.data.provider.hc.HcStepProvider
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.health.connect.client.HealthConnectClient
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveMonitoringClient
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {


    @Provides @Singleton
    fun provideStepProvider(@ApplicationContext ctx: Context) =
        HcStepProvider(ctx)

    @Provides @Singleton
    fun provideHrProvider(@ApplicationContext ctx: Context) =
        HcHeartRateProvider(ctx)

    /* ─── Health Services (stream) ─── */
    @Provides @Singleton
    fun providePassiveClient(@ApplicationContext ctx: Context): PassiveMonitoringClient =
        HealthServices.getClient(ctx).passiveMonitoringClient   // API pasiva:contentReference[oaicite:5]{index=5}

    /* ─── Data Layer ─── */
    @Provides @Singleton
    fun provideDataClient(@ApplicationContext ctx: Context): DataClient =
        Wearable.getDataClient(ctx)                             // instanciación oficial:contentReference[oaicite:6]{index=6}

    @Provides @Singleton
    fun provideCommDataSource(
        dataClient: DataClient,
        json: Json
    ) = WearableApiCommDataSource(dataClient, json)

    @Provides @Singleton
    fun provideJson(): Json =
        Json {                           // puedes ajustar config aquí
            ignoreUnknownKeys = true     // ejemplo: ignora campos extra
        }
}
