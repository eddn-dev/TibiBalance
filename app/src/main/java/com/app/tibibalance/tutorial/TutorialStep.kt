package com.app.tibibalance.tutorial

/**
 * Data that defines a single tutorial step.
 */
data class TutorialStepData(
    val id: String,
    val title: String,
    val message: String,
    val targetId: String?,
    val conditionalCheck: (suspend () -> Boolean)? = null
)
