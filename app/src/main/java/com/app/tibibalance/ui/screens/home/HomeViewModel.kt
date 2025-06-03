/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.DailyTip
import com.app.domain.entities.User
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.activity.ObserveActivitiesByDate
import com.app.domain.usecase.activity.RegisterActivityProgress
import com.app.domain.usecase.dailytips.GetTodayTipUseCase
import com.app.domain.usecase.wear.ObserveWatchConnectionUseCase
import com.app.domain.usecase.user.ObserveUser
import com.app.tibibalance.ui.screens.home.activities.ActivityUi
import com.app.tibibalance.ui.components.inputs.iconByName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    observeActsByDate       : ObserveActivitiesByDate,
    private val habitRepo   : HabitRepository,                // para nombre + icono
    private val registerProgress: RegisterActivityProgress,
    authUidProvider         : AuthUidProvider
) : ViewModel() {

    private val user: Flow<User> = observeUser(authUidProvider())
    private val _selectedActivity = MutableStateFlow<ActivityUi?>(null)
    val selectedActivity: StateFlow<ActivityUi?> = _selectedActivity

    fun openLog(act: ActivityUi) { _selectedActivity.value = act }
    fun dismissLog() { _selectedActivity.value = null }

    fun saveProgress(id: ActivityId, qty: Int?, status: ActivityStatus) =
        viewModelScope.launch {
            registerProgress(id, qty, status)
            _selectedActivity.value = null
        }

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

    val ui: StateFlow<HomeUi> = combine(
        user, tip, todayActivities, selectedActivity
    ) { u, t, a, sel ->
        HomeUi(user = u, dailyTip = t, activities = a, selectedActivity = sel)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())
}


data class HomeUi(
    val user            : User? = null,
    val dailyTip        : DailyTip? = null,
    val activities      : List<ActivityUi> = emptyList(),
    val selectedActivity: ActivityUi? = null
)

