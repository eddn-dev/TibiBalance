package com.app.tibibalance.tutorial

/**
 * Provides the list of tutorial steps shown to the user.
 */
object TutorialSteps {
    val all = listOf(
        TutorialStepData(
            id = "intro",
            title = "¡Bienvenido a TibiBalance!",
            message = "Te guiaré rápidamente por las funciones principales para que aproveches al máximo la app.",
            targetId = null
        ),
        TutorialStepData(
            id = "habits_tab",
            title = "¡Empieza a Crear Hábitos Saludables!",
            message = "TibiBalance te ayuda a construir rutinas positivas. Aquí gestionarás todos tus hábitos. ¡Comencemos creando uno!",
            targetId = "habits_tab"
        ),
        TutorialStepData(
            id = "habit_fab",
            title = "Crea tu primer hábito.",
            message = "Toca aquí para añadir un nuevo hábito. Si quieres que la aplicación registre tu progreso automáticamente, te recomendamos crear un Hábito en Modo Reto con una cantidad.",
            targetId = "habit_fab"
        ),
        TutorialStepData(
            id = "daily_progress",
            title = "Tu progreso diario.",
            message = "Aquí verás un resumen de tu progreso diario. Para tus hábitos en 'Modo Reto', la aplicación registrará automáticamente tus avances.",
            targetId = "daily_progress_card"
        ),
        TutorialStepData(
            id = "activity_fab",
            title = "Registra tus actividades completadas.",
            message = "Toca este botón para registrar una actividad completada. Es especialmente útil para tus hábitos en 'Modo Reto' que requieren un registro manual, o para registrar tu estado de ánimo diario.",
            targetId = "activity_fab"
        ),
        TutorialStepData(
            id = "daily_tip",
            title = "Consejos diarios para ti.",
            message = "Obtén un consejo diario para mantener tu motivación y bienestar. ¡Un pequeño empujón cada día!",
            targetId = "daily_tip_card"
        ),
        TutorialStepData(
            id = "stats",
            title = "Analiza tu progreso y conecta tu SmartWatch.",
            message = "Explora tus estadísticas y visualiza tu progreso a lo largo del tiempo. Para obtener métricas más detalladas como pasos o ritmo cardíaco, puedes conectar tu smartwatch en la sección de Ajustes.",
            targetId = "connect_watch_card"
        ),
        TutorialStepData(
            id = "profile",
            title = "Accede a tu perfil y logros.",
            message = "En tu perfil, puedes editar tu información, como tu nombre y foto. Explora la sección de Logros para ver las insignias que has conseguido y las que te esperan. ¡Cada meta que alcances te dará una nueva insignia!",
            targetId = "profile_section"
        ),
        TutorialStepData(
            id = "settings",
            title = "Ajustes generales de la aplicación.",
            message = "En esta sección, podrás personalizar la aplicación a tu gusto, congurar noticaciones, cambiar el tema y mucho más.",
            targetId = "settings_section"
        ),
        TutorialStepData(
            id = "final",
            title = "¡Tutorial completado!",
            message = "¡Eso es todo por ahora! Ya estás listo para usar TibiBalance al máximo. Recuerda que siempre puedes encontrar este tutorial completo en el botón de 'Ayuda' y que existen mensajes de ayuda contextuales en varias secciones de la aplicación para servirte de guía. ¡Estamos aquí para ayudarte a alcanzar tus metas!",
            targetId = null
        )
    )
}
