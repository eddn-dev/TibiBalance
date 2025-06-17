package com.app.tibibalance.tutorial

object TutorialSteps {

    val home = listOf(
        TutorialStepData(
            id = "intro",
            title = "¡Bienvenido a TibiBalance!",
            message = "Aquí verás tu progreso diario y las actividades.\nTe guiaré rápidamente por las funciones principales.",
            targetId = null,
            layout = TutorialLayout.CenteredIntro
        ),
        TutorialStepData(
            id = "daily_progress",
            title = "Tu progreso diario",
            message = "Aquí verás un resumen de tu progreso diario y tus actividades pendientes.\n¡Mantente al día con tus metas!",
            targetId = "daily_progress_card",
            layout = TutorialLayout.CenteredDialog
        ),
        TutorialStepData(
            id = "daily_tip",
            title = "Consejo diario",
            message = "Obtén un consejo diario para mantener tu motivación y bienestar.\n¡Un pequeño empujón cada día!",
            targetId = "daily_tip_card",
            layout = TutorialLayout.BottomRight
        ),
        TutorialStepData(
            id = "navigation",
            title = "Navegación principal",
            message = "Utiliza esta barra de navegación para moverte entre las secciones clave de la aplicación.\nTe recomendamos visitar la sección de Hábitos para comenzar.",
            targetId = "bottom_nav_bar",
            layout = TutorialLayout.VideoDialog
        ),
        TutorialStepData(
            id = "final_home",
            title = "¡Recorrido completado!",
            message = "Puedes seguir explorando la aplicación y encontrar más tutoriales.\nSi en algún momento deseas repasar estos tutoriales, puedes oprimir el botón de ayuda que aparece en cada sección.",
            targetId = null,
            layout = TutorialLayout.FinalMessage
        )
    )

    val habits = listOf(
        TutorialStepData(
            id = "habits_tab",
            title = "Gestión y creación de hábitos",
            message = "Esta es tu sección de Hábitos.\nAquí puedes gestionar todos tus hábitos, crear nuevos personalizados o en 'Modo Reto' para un seguimiento más detallado.",
            targetId = "habits_tab",
            layout = TutorialLayout.CenteredDialog
        ),
        TutorialStepData(
            id = "habit_fab",
            title = "Crea un nuevo hábito",
            message = "Toca aquí para añadir un nuevo hábito.\nPara registro automático, te recomendamos crear uno en 'Modo Reto' con una cantidad definida.",
            targetId = "habit_fab",
            layout = TutorialLayout.VideoDialog
        )
    )

    val emotions = listOf(
        TutorialStepData(
            id = "emotion_calendar_day",
            title = "Registro emocional diario",
            message = "En este calendario emocional, puedes registrar cómo te sientes cada día.\nPresiona sobre el día actual para registrar tu emoción.",
            targetId = "calendar_today",
            layout = TutorialLayout.CenteredDialog
        ),
        TutorialStepData(
            id = "emotion_history",
            title = "Historial y resumen emocional",
            message = "Verás tu historial de emociones y, al final del mes, se identificará la emoción más frecuente.\nEsto te ayuda a entender mejor tu bienestar.",
            targetId = "emotion_summary",
            layout = TutorialLayout.VideoDialog
        )
    )

    val stats = listOf(
        TutorialStepData(
            id = "stats",
            title = "Analiza tu progreso",
            message = "Registra tu monitoreo físico vincula tu reloj para obtener gráficas de tus métricas.",
            targetId = "stats_graph",
            layout = TutorialLayout.CenteredDialog
        ),
        TutorialStepData(
            id = "smartwatch",
            title = "Conecta tu smartwatch",
            message = "Para obtener métricas como pasos o ritmo cardíaco, puedes conectar tu smartwatch desde la sección de Ajustes.",
            targetId = "connect_watch_card",
            layout = TutorialLayout.VideoDialog
        )
    )

    val settings = listOf(
        TutorialStepData(
            id = "settings",
            title = "Personaliza tu experiencia",
            message = "Desde aquí, personaliza TibiBalance para que se adapte perfectamente a ti.\nTienes el control total sobre tu experiencia.",
            targetId = "settings_section",
            layout = TutorialLayout.CenteredDialog
        ),
        TutorialStepData(
            id = "profile",
            title = "Tu perfil y logros",
            message = "Gestiona tu información de perfil y explora tus logros.\nCada insignia representa un hito en tu camino hacia el equilibrio.",
            targetId = "profile_section",
            layout = TutorialLayout.BottomRight
        )
    )
}