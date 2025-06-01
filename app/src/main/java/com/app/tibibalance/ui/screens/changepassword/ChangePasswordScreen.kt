/* ui/screens/changepassword/ChangePasswordScreen.kt */
package com.app.tibibalance.ui.screens.changepassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.R
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.containers.ImageContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.inputs.InputPassword
import com.app.tibibalance.ui.components.layout.Header
import com.app.tibibalance.ui.components.texts.Description
import com.app.tibibalance.ui.components.utils.gradient

@Composable
fun ChangePasswordScreen(
    navController: NavHostController,
    viewModel   : ChangePasswordViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    /* ---- diálogos (mantengo la misma UX) ---- */
    var showErrorDialog   by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMsg         by remember { mutableStateOf("") }

    /* Observa errores */
    LaunchedEffect(ui.error) {
        ui.error?.let { err ->
            dialogMsg = err
            showErrorDialog = true
            viewModel.consumeError()
        }
    }
    /* Observa éxito */
    LaunchedEffect(ui.success) {
        if (ui.success) {
            dialogMsg = "¡Contraseña cambiada con éxito!"
            showSuccessDialog = true
            viewModel.clearSuccess()
        }
    }


    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(gradient())
                .verticalScroll(rememberScrollState())
                .padding(top = 100.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageContainer(
                resId = R.drawable.ic_resetpassword,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 24.dp)
            )

            Description(
                text      = "Ingresa tu nueva contraseña",
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                InputPassword(
                    value = ui.current,
                    onValueChange = viewModel::onCurrentChange,
                    label = "Contraseña Actual"
                )
                Spacer(Modifier.height(8.dp))

                InputPassword(
                    value = ui.newPass,
                    onValueChange = viewModel::onNewChange,
                    label = "Nueva Contraseña",
                    isError = ui.strengthError != null,
                    supportingText = ui.strengthError
                )
                Spacer(Modifier.height(8.dp))

                InputPassword(
                    value = ui.confirm,
                    onValueChange = viewModel::onConfirmChange,
                    label = "Confirmar Contraseña",
                    isError = ui.mismatchError != null,
                    supportingText = ui.mismatchError
                )
            }

            Spacer(Modifier.height(32.dp))

            val saveEnabled = !ui.isLoading &&
                    ui.current.isNotBlank() &&
                    ui.strengthError == null &&
                    ui.mismatchError == null

            PrimaryButton(
                text = if (ui.isLoading) "Cambiando..." else "Guardar",
                onClick = viewModel::changePassword,
                enabled = saveEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }

        Header(
            title = "Cambiar Contraseña",
            showBackButton = true,
            onBackClick = { navController.navigateUp() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        ModalInfoDialog(
            visible = showErrorDialog,
            message = dialogMsg,
            primaryButton = DialogButton("Aceptar") { showErrorDialog = false }
        )

        ModalInfoDialog(
            visible = showSuccessDialog,
            message = dialogMsg,
            primaryButton = DialogButton("Aceptar") {
                showSuccessDialog = false
                navController.popBackStack()   // ChangePasswordScreen
                navController.popBackStack()   // SettingsScreen
            }
        )
    }
}
