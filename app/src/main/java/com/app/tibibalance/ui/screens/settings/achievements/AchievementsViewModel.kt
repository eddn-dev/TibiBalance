/* ui/screens/settings/achievements/AchievementsViewModel.kt */
package com.app.tibibalance.ui.screens.settings.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.entities.Achievement
import com.app.domain.usecase.achievement.ObserveAchievements
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    observeAchievements: ObserveAchievements
) : ViewModel() {

    /**
     * Mapa inmutable id → Achievement siempre actualizado desde ROOM.
     * El Worker que ya creaste se encarga de sincronizar con Firestore;
     * la UI no toca la red.
     */
    val achievements: StateFlow<Map<String, Achievement>> =
        observeAchievements()                         // Flow<List<Achievement>>
            .map { list -> list.associateBy { it.id.raw } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyMap()
            )
}
