/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.DailyTip
import com.app.domain.entities.DashboardSnapshot
import com.app.domain.entities.User
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.repository.HabitRepository
import com.app.domain.usecase.activity.ObserveActivitiesByDate
import com.app.domain.usecase.activity.RegisterActivityProgress
import com.app.domain.usecase.dailytips.GetTodayTipUseCase
import com.app.domain.usecase.metrics.ObserveDashboard
import com.app.domain.usecase.user.ObserveUser
import com.app.tibibalance.ui.components.inputs.iconByName
import com.app.tibibalance.ui.permissions.HEALTH_CONNECT_READ_PERMISSIONS
import com.app.tibibalance.ui.screens.home.activities.ActivityUi
import com.app.tibibalance.utils.HealthConnectAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    // ─── casos de uso existentes ───────────────────────────────────────────
    getTodayTip              : GetTodayTipUseCase,
    observeUser              : ObserveUser,
    observeActsByDate        : ObserveActivitiesByDate,
    private val habitRepo    : HabitRepository,
    private val registerProgress : RegisterActivityProgress,

    // ─── NUEVO: flujo con las métricas ─────────────────────────────────────
    private val observeDashboard : ObserveDashboard,      // <- NUEVO

    // ─── Health Connect ────────────────────────────────────────────────────
    private val hcClient     : HealthConnectClient,
    hcAvailability           : HealthConnectAvailability,

    authUidProvider          : AuthUidProvider
) : ViewModel() {

    /*  Health Connect disponible en el dispositivo  */
    private val _hcAvailable = MutableStateFlow(hcAvailability.isHealthConnectReady())
    val hcAvailable: StateFlow<Boolean> = _hcAvailable

    /*  Permisos leídos/otorgados  */
    private val _healthPermsGranted = MutableStateFlow<Boolean?>(null)
    val healthPermsGranted: StateFlow<Boolean?> = _healthPermsGranted

    /*  Snapshot del dashboard (pasos, kcal, FC…)  */
    private val _dashboardSnapshot = MutableStateFlow<DashboardSnapshot?>(null)
    val dashboardSnapshot: StateFlow<DashboardSnapshot?> = _dashboardSnapshot


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

    init {
        /*  Cuando HC está disponible Y los permisos se confirman → empezar a
            escuchar el flujo observeDashboard(); si no, detenerlo.              */
        viewModelScope.launch {
            combine(
                _hcAvailable,
                _healthPermsGranted.filterNotNull()
            ) { hcOk, permsOk -> hcOk && permsOk }
                .distinctUntilChanged()
                .flatMapLatest { enabled ->
                    if (enabled) observeDashboard()       // Flow<DashboardSnapshot>
                    else flowOf(null)                     // deja snapshot en null
                }
                .collect { snap -> _dashboardSnapshot.value = snap }
        }
    }

    /* --------------------------------------------------------------------- */
    /*   REFRESH & CALLBACKS                                                 */
    /* --------------------------------------------------------------------- */

    fun refreshHealthPermissions() = viewModelScope.launch {
        _healthPermsGranted.value = hasAllHealthPermissions()
    }

    private suspend fun hasAllHealthPermissions(): Boolean =
        hcClient.permissionController
            .getGrantedPermissions()
            .containsAll(HEALTH_CONNECT_READ_PERMISSIONS)

    fun onPermissionsResult(granted: Boolean) {
        _healthPermsGranted.value = granted          //                          ⬅️
        // No hace falta iniciar nada aquí; el lanzador de init{} reaccionará
    }

    /* --------------------------------------------------------------------- */
    /*   UI STATE                                                            */
    /* --------------------------------------------------------------------- */

    private val baseUi = combine(
        user,
        tip,
        todayActivities,
        selectedActivity,
        _hcAvailable
    ) { u, t, acts, sel, hcAvail ->
        PartialUi(u, t, acts, sel, hcAvail)
    }

    val ui: StateFlow<HomeUi> = combine(
        baseUi,
        _healthPermsGranted.filterNotNull(),
        _dashboardSnapshot                       // <- añadido
    ) { partial, permsOk, dash ->
        HomeUi(
            user                = partial.user,
            dailyTip            = partial.tip,
            activities          = partial.activities,
            selectedActivity    = partial.selectedActivity,
            hcAvailable         = partial.hcAvailable,
            healthPermsGranted  = permsOk,
            dashboardSnapshot   = dash              // <- nuevo campo
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())
}


data class HomeUi(
    val user               : User? = null,
    val dailyTip           : DailyTip? = null,
    val activities         : List<ActivityUi> = emptyList(),
    val selectedActivity   : ActivityUi? = null,
    val hcAvailable        : Boolean = true,
    val healthPermsGranted : Boolean = false,
    val dashboardSnapshot  : DashboardSnapshot? = null
)


private data class PartialUi(
    val user: User?,
    val tip : DailyTip?,
    val activities: List<ActivityUi>,
    val selectedActivity: ActivityUi?,
    val hcAvailable: Boolean
)
