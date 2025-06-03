/* :data/repository/WearConnectionRepositoryImpl.kt */
package com.app.data.repository

import android.content.Context
import com.app.domain.repository.WearConnectionRepository
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val POLL_MS = 10_000L        // ↺ cada 10 s

@Singleton
class WearConnectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val ctx: Context
) : WearConnectionRepository {

    override fun isWatchConnected(): Flow<Boolean> = callbackFlow {
        val client: NodeClient = Wearable.getNodeClient(ctx)

        suspend fun sampleOnce() {
            val nodes = runCatching { client.connectedNodes.await() }.getOrNull().orEmpty()
            trySend(nodes.any { it.isNearby })
        }

        // muestreo inicial
        sampleOnce()

        // muestreo periódico
        val ticker = launch {
            while (isActive) {
                delay(POLL_MS)
                sampleOnce()
            }
        }

        awaitClose { ticker.cancel() }
    }
}
