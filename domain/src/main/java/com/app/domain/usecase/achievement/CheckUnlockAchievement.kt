package com.app.domain.usecase.achievement

import com.app.domain.achievements.event.AchievementEvent
import com.app.domain.entities.Achievement
import com.app.domain.enums.Emotion
import com.app.domain.enums.HabitCategory
import com.app.domain.ids.AchievementId
import com.app.domain.repository.AchievementsRepository
import com.app.domain.repository.EmotionRepository
import com.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import javax.inject.Inject

class CheckUnlockAchievement @Inject constructor(
    private val repo        : AchievementsRepository,
    private val habitRepo   : HabitRepository,
    private val emotionRepo : EmotionRepository
) {

    suspend operator fun invoke(ev: AchievementEvent): List<Achievement> {
        val unlocked = mutableListOf<Achievement>()

        when (ev) {
            /* ─── HABIT ADDED ─── */
            is AchievementEvent.HabitAdded -> {
                unlockByCategory(ev.category, unlocked)
                unlockChallenges(ev.isChallenge, unlocked)
            }

            /* ─── EMOTION ─── */
            is AchievementEvent.EmotionLogged -> {
                unlockHappy7Days(unlocked)
                unlock30Days(unlocked)
            }

            /* ─── PROFILE ─── */
            is AchievementEvent.ProfileUpdated ->
                if (ev.changedPhoto)
                    tryUnlock("foto_perfil", unlocked)

            /* ─── SETTINGS ─── */
            AchievementEvent.NotifCustomized ->
                tryUnlock("noti_personalizada", unlocked)
        }
        return unlocked
    }

    /* ───────────────── helpers ───────────────── */

    private suspend fun unlockByCategory(cat: HabitCategory, out: MutableList<Achievement>) {
        val id = when (cat) {
            HabitCategory.SALUD         -> "tibio_salud"
            HabitCategory.PRODUCTIVIDAD -> "tibio_productividad"
            HabitCategory.BIENESTAR     -> "tibio_bienestar"
            else                        -> return
        }
        tryUnlock(id, out)
    }

    private suspend fun unlockChallenges(isChallenge: Boolean, out: MutableList<Achievement>) {
        if (!isChallenge) return

        val retos = habitRepo.observeUserHabits().first().count { it.challenge != null }

        // progreso para “cinco_habitos”
        repo.updateProgress(AchievementId("cinco_habitos"), retos * 20)
        if (retos == 1) tryUnlock("primer_habito", out)
        if (retos >= 5) tryUnlock("cinco_habitos", out)
    }

    private suspend fun unlockHappy7Days(out: MutableList<Achievement>) {
        val happyDates = emotionRepo.observeAll().first()
            .filter { it.mood == Emotion.FELICIDAD }
            .map { it.date }
        repo.updateProgress(AchievementId("feliz_7_dias"), streak(happyDates, 7) * 100 / 7)
        if (streak(happyDates, 7) >= 7) tryUnlock("feliz_7_dias", out)
    }

    private suspend fun unlock30Days(out: MutableList<Achievement>) {
        val allDates = emotionRepo.observeAll().first().map { it.date }
        repo.updateProgress(AchievementId("emociones_30_dias"), streak(allDates, 30) * 100 / 30)
        if (streak(allDates, 30) >= 30) tryUnlock("emociones_30_dias", out)
    }

    private suspend fun tryUnlock(idStr: String, out: MutableList<Achievement>) {
        val id   = AchievementId(idStr)
        val achv = repo.find(id) ?: return
        if (achv.unlocked) return

        val now = Clock.System.now()
        val new = achv.copy(
            unlocked   = true,
            progress   = 100,
            unlockDate = now,
            meta       = achv.meta.copy(updatedAt = now, pendingSync = true)
        )
        repo.upsert(new)
        out += new
    }

    private fun streak(dates: List<LocalDate>, max: Int): Int {
        val sorted = dates.distinct().sorted()
        var streak = 1; var maxStreak = 1
        for (i in 1 until sorted.size)
            if (sorted[i-1].plusDays(1) == sorted[i]) maxStreak = maxOf(maxStreak, ++streak)
            else streak = 1
        return maxStreak.coerceAtMost(max)
    }

    //plusDays
    private fun LocalDate.plusDays(days: Int): LocalDate {
        return this.plus(days, DateTimeUnit.DAY)
    }
}
