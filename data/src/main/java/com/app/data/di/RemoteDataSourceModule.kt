// data/src/main/java/com/app/data/di/RemoteDataSourceModule.kt
package com.app.data.di

import com.app.data.remote.datasourcemetrics.MetricsRemoteDataSource
import com.app.data.remote.datasourcemetrics.MetricsRemoteDataSourceImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {

    /** Vincula la interfaz con su implementación */
    @Binds
    @Singleton
    abstract fun bindMetricsRemoteDataSource(
        impl: MetricsRemoteDataSourceImpl
    ): MetricsRemoteDataSource

    companion object {

        /** Provee la implementación concreta usando Firestore + userId lambda */
        @Provides
        @Singleton
        fun provideMetricsRemoteDataSourceImpl(
            firestore: FirebaseFirestore,
            firebaseAuth: FirebaseAuth
        ): MetricsRemoteDataSourceImpl {
            // Lambda para obtener el userId actual (null si no hay sesión)
            val userIdProvider: () -> String? = { firebaseAuth.currentUser?.uid }
            return MetricsRemoteDataSourceImpl(firestore, userIdProvider)
        }
    }
}
