package com.app.tibibalance.ui.screens.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.tibibalance.ui.navigation.Screen

@Composable
fun LaunchScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Launch Screen")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate(Screen.SignIn.route) }) {
            Text("Sign In")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate(Screen.SignUp.route) }) {
            Text("Sign Up")
        }
    }
}