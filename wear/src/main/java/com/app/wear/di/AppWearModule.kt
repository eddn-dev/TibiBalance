package com.app.wear.di

import android.content.Context
import com.app.wear.data.datasource.*
import com.app.wear.data.repository.WearHabitRepositoryImpl
import com.app.wear.data.repository.WearMetricsRepositoryImpl
import com.app.wear.domain.repository.IWearHabitRepository
import com.app.wear.domain.repository.IWearMetricsRepository
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppWearModule { // Renombrado para mayor claridad

    // --- Clientes de Wearable API (ya los tenías) ---
    @Provides
    @Singleton
    fun provideDataClient(@ApplicationContext context: Context): DataClient =
        Wearable.getDataClient(context)

    @Provides
    @Singleton
    fun provideMessageClient(@ApplicationContext context: Context): MessageClient =
        Wearable.getMessageClient(context)

    @Provides
    @Singleton
    fun provideNodeClient(@ApplicationContext context: Context): NodeClient =
        Wearable.getNodeClient(context)

    // --- Firebase (ya los tenías) ---
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- Serialización ---
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // Útil si los payloads evolucionan
        isLenient = true
        prettyPrint = false // Para producción, no necesitas pretty print
    }

    // --- DataSources ---
    @Provides
    @Singleton
    fun provideSensorDataSource(@ApplicationContext context: Context): ISensorDataSource {
        // Aquí decides qué implementación usar. Para Health Services:
        return HealthServicesSensorDataSource(context)
        // Si usaras SensorManager directamente:
        // return AndroidSensorManagerDataSource(context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
    }

    @Provides
    @Singleton
    fun provideCommunicationDataSource(dataClient: DataClient, json: Json): ICommunicationDataSource {
        return WearableApiCommDataSource(dataClient, json)
    }

    // --- Repositorios ---
    @Provides
    @Singleton
    fun provideWearMetricsRepository(
        sensorDataSource: ISensorDataSource,
        communicationDataSource: ICommunicationDataSource
    ): IWearMetricsRepository {
        return WearMetricsRepositoryImpl(sensorDataSource, communicationDataSource)
    }

    @Provides
    @Singleton
    fun provideWearHabitRepository( // Si lo implementas
        communicationDataSource: ICommunicationDataSource
        // , localDataSource: IWearLocalHabitDataSource // Si tienes caché local
    ): IWearHabitRepository {
        return WearHabitRepositoryImpl(communicationDataSource /*, localDataSource */)
    }

    // Los Casos de Uso y ViewModels se inyectan directamente vía constructor (@Inject)
    // si sus dependencias están disponibles en el grafo de Hilt.
}
