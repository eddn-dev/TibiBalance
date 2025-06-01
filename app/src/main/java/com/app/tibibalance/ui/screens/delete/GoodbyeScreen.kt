package com.app.tibibalance.ui.screens.delete

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import com.app.tibibalance.ui.navigation.Screen
import com.app.tibibalance.R

@Composable
fun GoodbyeScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1000) // Espera 1 segundo
        navController.navigate(Screen.Launch.route) {
            popUpTo(Screen.Launch.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_erased),
                contentDescription = "Adiós",
                modifier = Modifier.size(200.dp)
            )
            Text("¡Hasta pronto!", style = MaterialTheme.typography.headlineSmall)
        }
    }
}