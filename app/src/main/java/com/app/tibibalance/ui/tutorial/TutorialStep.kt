package com.app.tibibalance.ui.tutorial

sealed class TutorialStep(val data: TutorialStepData) {
    data object Intro : TutorialStep(
        TutorialStepData(
            id = "intro",
            title = "¡Bienvenido a TibiBalance!",
            message = "Conoce lo básico para empezar",
            targetId = null
        )
    )

    data object HabitsTab : TutorialStep(
        TutorialStepData(
            id = "habits_tab",
            title = "Empieza a Crear Hábitos",
            message = "Aquí gestionarás todos tus hábitos",
            targetId = "habits_tab"
        )
    )

    data object CreateHabit : TutorialStep(
        TutorialStepData(
            id = "create_habit",
            title = "Crea Tu Primer Hábito",
            message = "Toca aquí para añadir un nuevo hábito",
            targetId = "habit_fab"
        )
    )

    data object DailyProgress : TutorialStep(
        TutorialStepData(
            id = "daily_progress",
            title = "Tu Progreso Diario",
            message = "Aquí verás un resumen de tu progreso diario",
            targetId = "daily_progress_card"
        )
    )

    data object ActivityFab : TutorialStep(
        TutorialStepData(
            id = "activity_fab",
            title = "Registra tus Actividades",
            message = "Toca este botón para registrar una actividad completada",
            targetId = "activity_fab"
        )
    )

    data object DailyTip : TutorialStep(
        TutorialStepData(
            id = "daily_tip",
            title = "Consejos Diarios",
            message = "Obtén un consejo diario para mantener tu motivación",
            targetId = "daily_tip_card"
        )
    )

    data object Stats : TutorialStep(
        TutorialStepData(
            id = "stats_section",
            title = "Analiza tu Progreso",
            message = "Explora tus estadísticas y visualiza tu progreso",
            targetId = "stats_section"
        )
    )

    data object Profile : TutorialStep(
        TutorialStepData(
            id = "profile_section",
            title = "Accede a tu Perfil",
            message = "En tu perfil, puedes editar tu información",
            targetId = "profile_section"
        )
    )

    data object Settings : TutorialStep(
        TutorialStepData(
            id = "settings_section",
            title = "Ajustes Generales",
            message = "Personaliza la aplicación a tu gusto",
            targetId = "settings_section"
        )
    )

    data object Final : TutorialStep(
        TutorialStepData(
            id = "final",
            title = "¡Eso es todo por ahora!",
            message = "Disfruta usando TibiBalance",
            targetId = null
        )
    )

    companion object {
        /**
         * Returns the ordered list of steps for the tutorial.
         *
         * @param hasChallenge lambda that returns true when the user has at least
         * one habit in challenge mode. Used to conditionally show certain steps.
         */
        fun all(hasChallenge: suspend () -> Boolean): List<TutorialStepData> = listOf(
            Intro.data,
            HabitsTab.data,
            CreateHabit.data,
            DailyProgress.data.copy(conditionalCheck = hasChallenge),
            ActivityFab.data.copy(conditionalCheck = hasChallenge),
            DailyTip.data,
            Stats.data,
            Profile.data,
            Settings.data,
            Final.data
        )
    }
}

data class TutorialStepData(
    val id: String,
    val title: String,
    val message: String,
    val targetId: String?,
    val conditionalCheck: (suspend () -> Boolean)? = null
)
