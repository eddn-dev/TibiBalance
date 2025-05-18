/*  ui/screens/habits/addHabitWizard/AddHabitModal.kt  */
@file:OptIn(ExperimentalFoundationApi::class)

package com.app.tibibalance.ui.screens.habits.addHabitWizard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.domain.config.RepeatPreset
import com.app.domain.repository.HabitTemplateRepository
import com.app.tibibalance.ui.components.buttons.PrimaryButton
import com.app.tibibalance.ui.components.buttons.SecondaryButton
import com.app.tibibalance.ui.components.containers.ModalContainer
import com.app.tibibalance.ui.components.dialogs.DialogButton
import com.app.tibibalance.ui.components.dialogs.ModalInfoDialog
import com.app.tibibalance.ui.components.utils.PagerIndicator
import com.app.tibibalance.ui.screens.habits.addHabitWizard.step.*
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddHabitModal(
    onDismissRequest: () -> Unit,
    vm: AddHabitViewModel = hiltViewModel()
) {

    /* ------------- estado observable del VM ------------- */
    val page          by vm.page.collectAsState()        // índice actual :contentReference[oaicite:6]{index=6}
    val formState     by vm.form.collectAsState()
    val notifState    by vm.notif.collectAsState()
    val templates     by vm.templates.collectAsState()

    /* ------------- escuche eventos one-shot (éxito / error) ------------- */
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        vm.events.collectLatest { ev ->
            when (ev) {
                WizardEvent.Saved  -> onDismissRequest()
                is WizardEvent.Error -> {
                    /* se muestra diálogo abajo; no cerramos */
                }
            }
        }
    }

    /* ------------- pager & sync con page Flow ------------- */
    val pagerState = rememberPagerState(initialPage = page, pageCount = { 4 }) // :contentReference[oaicite:7]{index=7}
    LaunchedEffect(page) { pagerState.animateScrollToPage(page) }              // :contentReference[oaicite:8]{index=8}

    /* ------------- altura máxima 85 % de pantalla ------------- */
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * .85f

    ModalContainer(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.heightIn(max = maxHeight),
        closeButtonEnabled = true
    ) {

        /* --------------- contenido principal --------------- */
        Column(Modifier.fillMaxSize()) {

            /* pager */
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                pageSize = PageSize.Fill
            ) { index ->
                when (index) {
                    0 -> SuggestionStep(
                        templates   = templates,
                        onSuggestion= vm::applyTemplate
                    )

                    1 -> BasicInfoStep(
                        initial      = formState,
                        errors       = vm.basicErrors(formState),
                        onFormChange = vm::updateBasic
                    )

                    2 -> TrackingStep(
                        initial      = formState,
                        errors       = emptyList(),      //  valida dentro del VM si lo necesitas
                        onFormChange = vm::updateTrack
                    )

                    3 -> NotificationStep(
                        title        = formState.name.ifBlank { "Notificación" },
                        initialCfg   = notifState,
                        onCfgChange  = vm::updateNotif
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            /* --------------- barra inferior de acciones --------------- */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                /* botón atrás */
                if (page > 0)
                    SecondaryButton(text = "Atrás", onClick = vm::back)

                Spacer(Modifier.width(8.dp))

                /* botón primario dinámico */
                when (page) {
                    0 -> PrimaryButton(
                        text = "Crear hábito vacío",
                        onClick = vm::next
                    )

                    1 -> PrimaryButton(
                        text    = "Siguiente",
                        enabled = vm.basicErrors(formState).isEmpty(),
                        onClick = vm::next
                    )

                    2 -> {
                        /** Si el usuario activó notificaciones + repeat definido ⇒ vamos a paso 3,
                         *  si no, guardamos directamente */
                        val needsNotif = formState.notify &&
                                formState.repeatPreset != RepeatPreset.INDEFINIDO
                        PrimaryButton(
                            text = if (needsNotif) "Siguiente" else "Guardar",
                            onClick = {
                                if (needsNotif) vm.next()
                                else vm.save()
                            }
                        )
                    }

                    3 -> PrimaryButton(text = "Guardar", onClick = vm::save)
                }
            }

            /* indicador de página */
            PagerIndicator(
                pagerState = pagerState,
                pageCount  = 4,
                modifier   = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp)
            )
        }
    }

    /* --------------- diálogos auxiliares (éxito / error) --------------- */
    val lastEvent = vm.events.collectAsState(initial = null).value
    when (lastEvent) {
        WizardEvent.Saved -> ModalInfoDialog(
            visible = true,
            icon    = Icons.Default.Check,
            title   = "¡Hábito guardado!",
            message = "Tu nuevo hábito se añadió correctamente."
        )

        is WizardEvent.Error -> ModalInfoDialog(
            visible       = true,
            icon          = Icons.Default.Error,
            iconColor     = MaterialTheme.colorScheme.error,
            title         = "Ups…",
            message       = lastEvent.message,
            primaryButton = DialogButton("Entendido") {}
        )

        else -> {}
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TemplateRepoEntryPoint {
    fun repo(): HabitTemplateRepository
}

@Composable
private fun rememberTemplates(): List<com.app.domain.entities.HabitTemplate> {
    val ctx  = LocalContext.current.applicationContext
    val repo = EntryPointAccessors.fromApplication(
        ctx, TemplateRepoEntryPoint::class.java
    ).repo()
    return repo.templates.collectAsState(initial = emptyList()).value
}
