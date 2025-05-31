package com.app.tibibalance.ui

import android.content.Intent // Added
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController // Added
// Assuming HabitAlertReceiver.HABIT_ID_INTENT_EXTRA_KEY is accessible.
// It was defined in data/alert/HabitAlertReceiver.kt.
// If data module is not directly accessible, this key needs to be in a common place or :app module.
// For now, assuming the direct import path or that the constant is copied/available.
// Let's use the constant defined in UncompletedHabitsWorker as it's in data module too and more recent.
import com.app.data.worker.UncompletedHabitsWorker // For HABIT_ID_INTENT_EXTRA_KEY (better than HabitAlertReceiver if it's also used by worker)
// Actually, HabitAlertReceiver is more appropriate if this is for *all* notifications that might carry habitId.
// Let's stick to HabitAlertReceiver as per the plan.
import com.app.data.alert.HabitAlertReceiver // For HABIT_ID_INTENT_EXTRA_KEY

import com.app.tibibalance.ui.navigation.AppNavGraph
import com.app.tibibalance.ui.theme.AppThemeViewModel
import com.app.tibibalance.ui.theme.TibiBalanceTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TibiBalanceRoot(activityIntent: Intent?) { // Modified signature
    val themeVm: AppThemeViewModel = hiltViewModel()
    val mode = themeVm.mode.collectAsState().value

    val navController = rememberNavController() // Create NavController here
    var startHabitId: String? = null

    if (activityIntent != null) {
        startHabitId = activityIntent.getStringExtra(HabitAlertReceiver.HABIT_ID_INTENT_EXTRA_KEY)
        if (startHabitId != null) {
            Log.d("TibiBalanceRoot", "Received startHabitId: $startHabitId from intent.")
            // Attempt to remove the extra to prevent re-processing on simple recomposition without new intent instance
            // This is a basic approach. For robustness, especially across config changes/process death,
            // this should be handled by a ViewModel and a "consumed" event state.
            activityIntent.removeExtra(HabitAlertReceiver.HABIT_ID_INTENT_EXTRA_KEY)
        }
    }

    TibiBalanceTheme(mode = mode) {
        AppNavGraph(navController = navController, startHabitId = startHabitId) // Pass both
    }
}
