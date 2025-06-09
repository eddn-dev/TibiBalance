// :app/sync/EmotionScheduleWorker.kt
package com.app.tibibalance.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.app.data.alert.EmotionAlertManager
import com.app.domain.usecase.emotion.HasEmotionEntryForDate
import com.app.domain.usecase.user.GetUserSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.util.concurrent.TimeUnit

@HiltWorker
class EmotionScheduleWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val getSettings   : GetUserSettings,
    private val hasEntry      : HasEmotionEntryForDate,
    private val alertMgr      : EmotionAlertManager
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val st = getSettings().first() ?: return@withContext Result.success()

        // 1️⃣  ¿el usuario tiene ON el recordatorio?
        if (!st.notifEmotion || st.notifEmotionTime == null) {
            alertMgr.cancel(); return@withContext Result.success()
        }

        // 2️⃣  ¿ya registró emoción hoy?
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (hasEntry(today)) { alertMgr.cancel(); return@withContext Result.success() }

        // 3️⃣  programar/actualizar la alarma
        // Notif emotion time es del tipo String
        st.notifEmotionTime!!.toLocalTimeOrNull()?.let { alertMgr.schedule(it) }
        Result.success()
    }

    companion object {

        private const val UNIQUE = "emotion_scheduler"

        /** se llama desde el módulo Hilt para dejar el worker planificado */
        fun periodicRequest() = PeriodicWorkRequestBuilder<EmotionScheduleWorker>(
            12, TimeUnit.HOURS       // revisa dos veces al día
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        fun enqueue(workManager: WorkManager) =
            workManager.enqueueUniquePeriodicWork(
                UNIQUE,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest()
            )
    }
}


/**
 * Convierte una cadena "HH:mm" a LocalTime.
 * @return null si el formato no es válido.
 */
fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
    LocalTime.parse(this)                 // kotlinx-datetime ≥ 0.5  acepta "HH:mm"
}.getOrNull()
