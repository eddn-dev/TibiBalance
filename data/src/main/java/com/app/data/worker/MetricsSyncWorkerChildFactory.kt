// data/src/main/java/com/app/data/worker/MetricsSyncWorkerChildFactory.kt
package com.app.data.worker
/*
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import javax.inject.Inject

/**
 * Adaptador que expone MetricsSyncWorker.Factory como ChildWorkerFactory,
 * de forma que el WorkerFactory global pueda instanciarlo.
 */
class MetricsSyncWorkerChildFactory @Inject constructor(
    private val assistedFactory: MetricsSyncWorker.Factory
) : ChildWorkerFactory {

    override fun create(appContext: Context, params: WorkerParameters): ListenableWorker {
        // Delegamos la creación al factory generado por Hilt
        return assistedFactory.create(appContext, params)
    }
}
*/