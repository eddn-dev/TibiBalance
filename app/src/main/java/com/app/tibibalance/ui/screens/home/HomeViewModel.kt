/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import android.health.connect.HealthConnectManager
import androidx.health.connect.client.HealthConnectClient
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
import com.app.domain.usecase.user.ObserveUser
import com.app.tibibalance.ui.screens.home.activities.ActivityUi
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.permissions.HEALTH_CONNECT_READ_PERMISSIONS
import com.app.tibibalance.utils.HealthConnectAvailability
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
    getTodayTip             : GetTodayTipUseCase,
    observeUser             : ObserveUser,
    observeActsByDate       : ObserveActivitiesByDate,
    private val habitRepo   : HabitRepository,                // para nombre + icono
    private val registerProgress: RegisterActivityProgress,
    private val hcClient    : HealthConnectClient,
    hcAvailability          : HealthConnectAvailability,
    authUidProvider         : AuthUidProvider
) : ViewModel() {

    private val _hcAvailable = MutableStateFlow(hcAvailability.isHealthConnectReady())
    val hcAvailable: StateFlow<Boolean> = _hcAvailable

    private val _healthPermsGranted = MutableStateFlow<Boolean?>(null)
    val healthPermsGranted: StateFlow<Boolean?> = _healthPermsGranted

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

    private val tip: Flow<DailyTip?> = getTodayTip()

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

    private val baseUi = combine(                     // 5 flows exactamente
        user,
        tip,
        todayActivities,
        selectedActivity,
        _hcAvailable
    ) { u, t, acts, sel, hcAvail ->
        PartialUi(u, t, acts, sel, hcAvail)           // data class auxiliar
    }

    val ui: StateFlow<HomeUi> = baseUi
        .combine(_healthPermsGranted.filterNotNull()) { partial, permsOk ->
            HomeUi(
                user               = partial.user,
                dailyTip           = partial.tip,
                activities         = partial.activities,
                selectedActivity   = partial.selectedActivity,
                hcAvailable        = partial.hcAvailable,
                healthPermsGranted = permsOk
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())


    fun refreshHealthPermissions() = viewModelScope.launch {
        _healthPermsGranted.value = hasAllHealthPermissions()
    }

    suspend fun hasAllHealthPermissions(): Boolean =
        hcClient.permissionController
            .getGrantedPermissions()
            .containsAll(HEALTH_CONNECT_READ_PERMISSIONS)

    fun onPermissionsResult(granted: Boolean) {
        _healthPermsGranted.value = granted
        if (granted) {
            // Aquí más adelante arrancarás ObserveDashboard  (fase 7)
        }
    }
}


data class HomeUi(
    val user               : User? = null,
    val dailyTip           : DailyTip? = null,
    val activities         : List<ActivityUi> = emptyList(),
    val selectedActivity   : ActivityUi? = null,
    val hcAvailable        : Boolean = true,
    val healthPermsGranted : Boolean = false
)

private data class PartialUi(
    val user: User?,
    val tip : DailyTip?,
    val activities: List<ActivityUi>,
    val selectedActivity: ActivityUi?,
    val hcAvailable: Boolean
)
