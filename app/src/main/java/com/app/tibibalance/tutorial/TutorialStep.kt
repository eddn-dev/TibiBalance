package com.app.tibibalance.tutorial

data class TutorialStepData(
    val id: String,
    val title: String,
    val message: String,
    val targetId: String?,
    val layout: TutorialLayout = TutorialLayout.CenteredDialog,
    val conditionalCheck: (suspend () -> Boolean)? = null
)
