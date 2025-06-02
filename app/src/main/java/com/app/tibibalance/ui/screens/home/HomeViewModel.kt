/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.DailyTip
import com.app.domain.entities.User
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.activity.ObserveActivitiesByDate
import com.app.domain.usecase.dailytips.GetTodayTipUseCase
import com.app.domain.usecase.wear.ObserveWatchConnectionUseCase
import com.app.domain.usecase.user.ObserveUser
import com.app.tibibalance.ui.components.feed.ActivityUi
import com.app.tibibalance.ui.components.inputs.iconByName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/* :app/ui/screens/home/HomeViewModel.kt */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeWatchConn        : ObserveWatchConnectionUseCase,
    getTodayTip             : GetTodayTipUseCase,
    observeUser             : ObserveUser,
    observeActsByDate       : ObserveActivitiesByDate,        // 👈 NUEVO use-case
    private val habitRepo   : HabitRepository,                // para nombre + icono
    authUidProvider         : AuthUidProvider
) : ViewModel() {

    private val user: Flow<User> = observeUser(authUidProvider())

    private val tip: Flow<DailyTip?> = combine(
        observeWatchConn(), getTodayTip()
    ) { watch, todayTip -> if (!watch) todayTip else null }

    /** Actividades del día enriquecidas con info del hábito. */
    private val todayActivities: Flow<List<ActivityUi>> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        emitAll(
            combine(
                observeActsByDate(today),
                habitRepo.observeUserHabits()
            ) { acts, habits ->
                val map = habits.associateBy { it.id }
                acts.mapNotNull { act ->
                    val parent = map[act.habitId] ?: return@mapNotNull null
                    ActivityUi(
                        act     = act,
                        name    = parent.name,
                        icon    = iconByName(parent.icon),
                    )
                }
            }
        )
    }

    val ui: StateFlow<HomeUi> = combine(user, tip, todayActivities) { u, t, a ->
        HomeUi(user = u, dailyTip = t, activities = a)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())
}


data class HomeUi(
    val user       : User? = null,
    val dailyTip   : DailyTip? = null,
    val activities : List<ActivityUi> = emptyList()
)
