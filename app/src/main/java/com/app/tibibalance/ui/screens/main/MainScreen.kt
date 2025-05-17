package com.app.tibibalance.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Import the padding modifier
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.app.tibibalance.ui.components.navigation.BottomNavBar
import com.app.tibibalance.ui.components.navigation.bottomItems
import com.app.tibibalance.ui.navigation.Screen

@Composable
fun MainScreen(rootNav: NavHostController) {
    val selected = remember { mutableStateOf(Screen.Home.route) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                items = bottomItems,
                selectedRoute = selected.value,
                onItemClick = { selected.value = it }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding), // <-- APPLY PADDING HERE
            contentAlignment = Alignment.Center
        ) {
            Text(text = selected.value)
        }
    }
}