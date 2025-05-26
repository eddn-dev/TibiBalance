/* ui/screens/profile/EditProfileRoute.kt */
package com.app.tibibalance.ui.screens.profile

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileRoute(
    nav: NavHostController,
    vm : EditProfileViewModel = hiltViewModel()
) {
    val ui by vm.state.collectAsState()

    /* ----- selector de imagen (launcher) ----- */
    var pendingCallback by remember {
        mutableStateOf<(Uri?) -> Unit>({})   // ✅ tipo explícito
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let(vm::pickPhoto)  // actualiza la preview global
        pendingCallback(uri)     // avisa al composable hijo
    }

    /* ----------- diálogo feedback ----------- */
    ModalInfoDialog(
        visible = ui.loading, loading = true
    )
    ModalInfoDialog(
        visible = ui.success,
        icon    = Icons.Default.Check,
        message = "Cambios guardados",
        primaryButton = DialogButton("Cerrar") {
            vm.consumeSuccess()
            nav.popBackStack()
        }
    )
    ModalInfoDialog(
        visible = ui.error != null,
        icon    = Icons.Default.Error,
        iconColor = MaterialTheme.colorScheme.error,
        message = ui.error,
        primaryButton = DialogButton("Cerrar") { vm.consumeError() }
    )

    /* ---------------- pantalla ---------------- */
    ui.user?.let { user ->
        EditProfileScreen(nav, vm)
    }
}
