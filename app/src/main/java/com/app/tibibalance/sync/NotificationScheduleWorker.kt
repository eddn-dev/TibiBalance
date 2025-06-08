package com.app.tibibalance.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.app.data.repository.IoDispatcher
import com.app.domain.service.AlertManager
import com.app.domain.usecase.activity.EnsureActivitiesForDate
import com.app.domain.usecase.activity.GenerateDailyActivities
import com.app.domain.usecase.habit.GetHabitsFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.util.concurrent.TimeUnit

/* :app/sync/NotificationScheduleWorker.kt */
@HiltWorker
class NotificationScheduleWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val getHabitsFlow  : GetHabitsFlow,
    private val alertMgr       : AlertManager,
    private val ensureToday    : EnsureActivitiesForDate,
    private val dailyGen       : GenerateDailyActivities,
    @IoDispatcher private val io: CoroutineDispatcher
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(io) {
        val tz     = TimeZone.currentSystemDefault()
        val today  = Clock.System.todayIn(tz)

        // 1️⃣  leer hábitos (Room)
        val habits = getHabitsFlow().first()

        habits.forEach { habit ->
            alertMgr.cancel(habit.id)                 // limpia siempre

            val cfg = habit.notifConfig
            val inRange = (cfg.startsAt == null || today >= cfg.startsAt!!) &&
                    (cfg.expiresAt == null || today <= cfg.expiresAt!!)

            if (cfg.enabled && inRange) {             // programa si procede
                alertMgr.schedule(habit)
            }
        }

        // 2️⃣  generar/asegurar actividades de retos (opcional)
        ensureToday(today)
        dailyGen(today.plus(DatePeriod(days = 1)))

        Result.success()
    }

    companion object {
        fun periodicRequest() = PeriodicWorkRequestBuilder<NotificationScheduleWorker>(
            1, TimeUnit.DAYS       // ejecuta una vez al día; cambia si lo prefieres
        ).setInitialDelay(10, TimeUnit.MINUTES) // arranca 10 min tras boot
            .addTag("notif_schedule")
            .build()
    }
}
