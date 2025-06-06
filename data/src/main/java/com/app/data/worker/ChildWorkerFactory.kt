// data/src/main/java/com/app/data/worker/ChildWorkerFactory.kt
package com.app.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

/**
 * Interfaz para que Hilt pueda inyectar Workers con AssistedInject.
 * Cada Worker que use @HiltWorker debe implementar su propio Factory que herede de esta interfaz.
 */
interface ChildWorkerFactory {
    /**
     * Crea la instancia de ListenableWorker (por ejemplo, MetricsSyncWorker).
     *
     * @param appContext El contexto de la aplicación.
     * @param params     Los parámetros del Worker.
     * @return Una instancia del Worker solicitada.
     */
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker
}