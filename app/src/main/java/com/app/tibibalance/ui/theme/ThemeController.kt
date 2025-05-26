/* ui/theme/ThemeController.kt */
package com.app.tibibalance.ui.theme

import com.app.domain.enums.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeController @Inject constructor() {

    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode          // read-only para la UI

    fun setMode(mode: ThemeMode) { _mode.value = mode }
}
