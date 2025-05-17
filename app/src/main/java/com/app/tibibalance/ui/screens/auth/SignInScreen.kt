package com.app.tibibalance.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SignInScreen(navController: NavController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Sign In Screen")
    }
}