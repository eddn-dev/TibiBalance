package com.app.wear.presentation.viewmodel

data class MetricsUiState(
    val steps: Int = 0,
    val heartRate: Int? = null,
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val lastSentStatus: String? = null
)