package com.app.wear.presentation.viewmodel

sealed interface UiEvent {
    data class Toast(val message: String) : UiEvent
}
