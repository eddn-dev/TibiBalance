/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.*
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
    // ─── Casos de uso ──────────────────────────────────────────────────────
    getTodayTip          : GetTodayTipUseCase,
    observeUser          : ObserveUser,
    observeActsByDate    : ObserveActivitiesByDate,
    private val habitRepo: HabitRepository,
    private val registerProgress: RegisterActivityProgress,
    private val observeDashboard: ObserveDashboard,

    // ─── Health Connect ───────────────────────────────────────────────────
    private val hcClient : HealthConnectClient?,   // <- nullable
    hcAvailability       : com.app.tibibalance.utils.HealthConnectAvailability,

    authUidProvider      : AuthUidProvider
) : ViewModel() {

    /* Health Connect realmente utilizable (cliente no nulo + SDK_AVAILABLE) */
    private val _hcAvailable = MutableStateFlow(
        hcClient != null && hcAvailability.isHealthConnectReady()
    )
    val healthConnectClient: HealthConnectClient? get() = hcClient
    val hcAvailable: StateFlow<Boolean> = _hcAvailable.asStateFlow()

    /* Permisos concedidos (null primero, luego true/false) */
    private val _healthPermsGranted = MutableStateFlow<Boolean?>(null)
    val healthPermsGranted: StateFlow<Boolean?> = _healthPermsGranted.asStateFlow()

    /* Snapshot de métricas */
    private val _dashboardSnapshot = MutableStateFlow<DashboardSnapshot?>(null)
    val dashboardSnapshot: StateFlow<DashboardSnapshot?> = _dashboardSnapshot.asStateFlow()

    /* Datos de usuario, actividades, etc. */
    private val user             = observeUser(authUidProvider())
    private val _selectedActivity = MutableStateFlow<ActivityUi?>(null)
    val selectedActivity: StateFlow<ActivityUi?> = _selectedActivity.asStateFlow()

    /* -------------------------- Acciones UI ---------------------------- */

    fun openLog(act: ActivityUi)  { _selectedActivity.value = act }
    fun dismissLog()              { _selectedActivity.value = null }

    fun saveProgress(id: ActivityId, qty: Int?, status: ActivityStatus) =
        viewModelScope.launch {
            registerProgress(id, qty, status)
            _selectedActivity.value = null
        }

    /* -------------------- Construcción de flows base ------------------- */

    private val tip = getTodayTip()

    private val todayActivities: Flow<List<ActivityUi>> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        emitAll(
            combine(
                observeActsByDate(today),
                habitRepo.observeUserHabits()
            ) { acts, habits ->
                val byId = habits.associateBy { it.id }
                acts.mapNotNull { act ->
                    val parent = byId[act.habitId] ?: return@mapNotNull null
                    ActivityUi(
                        act   = act,
                        name  = parent.name,
                        icon  = iconByName(parent.icon)
                    )
                }
            }
        )
    }

    /* -------------------------- Init / binding ------------------------- */

    init {
        // 1) Detectar permisos al arrancar si HC existe
        refreshHealthPermissions()

        // 2) Cuando (HC disponible && permisos) cambie → suscribir métricas
        viewModelScope.launch {
            combine(
                _hcAvailable,
                _healthPermsGranted.filterNotNull()
            ) { hcOk, permsOk -> hcOk && permsOk }
                .distinctUntilChanged()
                .flatMapLatest { enabled ->
                    if (enabled) observeDashboard() else flowOf(null)
                }
                .collect { snap -> _dashboardSnapshot.value = snap }
        }
    }

    /* -------------------------- Permisos HC ---------------------------- */

    fun refreshHealthPermissions() = viewModelScope.launch {
        _healthPermsGranted.value = hasAllHealthPermissions()
    }

    private suspend fun hasAllHealthPermissions(): Boolean =
        hcClient
            ?.permissionController
            ?.getGrantedPermissions()
            ?.containsAll(HEALTH_CONNECT_READ_PERMISSIONS)
            ?: false                  // Cliente inexistente ⇒ permisos “no”

    /* Callback tras pedir permisos desde la UI */
    fun onPermissionsResult(granted: Boolean) {
        _healthPermsGranted.value = granted
        // El flujo init{} reaccionará; no lanzamos nada manualmente.
    }

    /* ------------------------------ UI -------------------------------- */

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
        _dashboardSnapshot
    ) { partial, permsOk, dash ->
        HomeUi(
            user               = partial.user,
            dailyTip           = partial.tip,
            activities         = partial.activities,
            selectedActivity   = partial.selectedActivity,
            hcAvailable        = partial.hcAvailable,
            healthPermsGranted = permsOk,
            dashboardSnapshot  = dash
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())
}

/* -------------------------- Modelos UI ------------------------------- */

data class HomeUi(
    val user               : User? = null,
    val dailyTip           : DailyTip? = null,
    val activities         : List<ActivityUi> = emptyList(),
    val selectedActivity   : ActivityUi? = null,
    val hcAvailable        : Boolean = false,
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
