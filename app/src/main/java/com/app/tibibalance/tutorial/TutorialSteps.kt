package com.app.tibibalance.tutorial

/**
 * Provides the list of tutorial steps shown to the user.
 */
object TutorialSteps {
    val all = listOf(
        TutorialStepData(
            id = "intro",
            title = "Bienvenido",
            message = "This tutorial will guide you through the app.",
            targetId = null
        ),
        TutorialStepData(
            id = "habits_tab",
            title = "Empieza a crear hábitos",
            message = "Open the Habits section from here.",
            targetId = "habits_tab"
        ),
        TutorialStepData(
            id = "habit_fab",
            title = "Crea tu primer hábito",
            message = "Tap the plus button to add a habit.",
            targetId = "habit_fab"
        ),
        TutorialStepData(
            id = "daily_progress",
            title = "Tu progreso diario",
            message = "Check your daily progress here.",
            targetId = "daily_progress_card"
        ),
        TutorialStepData(
            id = "activity_fab",
            title = "Registra actividades",
            message = "Log your habit activities.",
            targetId = "activity_fab"
        ),
        TutorialStepData(
            id = "daily_tip",
            title = "Consejo diario",
            message = "Read helpful tips every day.",
            targetId = "daily_tip_card"
        ),
        TutorialStepData(
            id = "stats",
            title = "Analiza tu progreso",
            message = "Connect your watch and view stats.",
            targetId = "connect_watch_card"
        ),
        TutorialStepData(
            id = "profile",
            title = "Accede a tu perfil",
            message = "Manage your achievements here.",
            targetId = "profile_section"
        ),
        TutorialStepData(
            id = "settings",
            title = "Ajustes generales",
            message = "Configure the app to your liking.",
            targetId = "settings_section"
        ),
        TutorialStepData(
            id = "final",
            title = "Listo",
            message = "Tutorial completed!",
            targetId = null
        )
    )
}
