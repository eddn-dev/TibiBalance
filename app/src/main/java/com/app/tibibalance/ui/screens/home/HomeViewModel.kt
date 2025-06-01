/* :app/ui/screens/home/HomeViewModel.kt */
package com.app.tibibalance.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.domain.auth.AuthUidProvider
import com.app.domain.entities.DailyTip
import com.app.domain.entities.User
import com.app.domain.usecase.dailytips.GetTodayTipUseCase
import com.app.domain.usecase.wear.ObserveWatchConnectionUseCase
import com.app.domain.usecase.dailytips.MarkTipShownUseCase
import com.app.domain.usecase.user.ObserveUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/* :app/ui/screens/home/HomeViewModel.kt */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeWatchConn : ObserveWatchConnectionUseCase,
    getTodayTip      : GetTodayTipUseCase,
    observeUser      : ObserveUser,
    authUidProvider  : AuthUidProvider
) : ViewModel() {

    private val user: Flow<User> = observeUser(authUidProvider())

    private val tip: Flow<DailyTip?> = combine(
        observeWatchConn(), getTodayTip()
    ) { watchConnected, todayTip ->
        if (!watchConnected) todayTip else null
    }

    val ui: StateFlow<HomeUi> = combine(user, tip) { u, t ->
        HomeUi(user = u, dailyTip = t)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUi())
}

data class HomeUi(
    val user: User? = null,
    val dailyTip: DailyTip? = null
)