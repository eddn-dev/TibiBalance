// :domain/achievements/DefaultAchievements.kt
package com.app.domain.achievements

import com.app.domain.entities.Achievement
import com.app.domain.ids.AchievementId

/**
 * Catálogo de logros iniciales que se insertan en Room la primera
 * vez que el usuario inicia sesión.  **No** cambies los IDs:
 * muchas reglas de negocio los usan para desbloqueo.
 */
object DefaultAchievements {

    val list: List<Achievement> = listOf(
        Achievement(
            id          = AchievementId("foto_perfil"),
            name        = "Un placer conocernos",
            description = "Cambia tu foto de perfil."
        ),
        Achievement(
            id          = AchievementId("tibio_salud"),
            name        = "Tibio saludable",
            description = "Agrega un hábito de salud."
        ),
        Achievement(
            id          = AchievementId("tibio_productividad"),
            name        = "Tibio productivo",
            description = "Agrega un hábito de productividad."
        ),
        Achievement(
            id          = AchievementId("tibio_bienestar"),
            name        = "Tibio del bienestar",
            description = "Agrega un hábito de bienestar."
        ),
        Achievement(
            id          = AchievementId("primer_habito"),
            name        = "El inicio del reto",
            description = "Agrega tu primer hábito con modo reto activado."
        ),
        Achievement(
            id          = AchievementId("cinco_habitos"),
            name        = "La sendera del reto",
            description = "Agrega cinco hábitos con modo reto activado."
        ),
        Achievement(
            id          = AchievementId("feliz_7_dias"),
            name        = "Todo en su lugar",
            description = "Registra un estado de ánimo “feliz” por siete días consecutivos."
        ),
        Achievement(
            id          = AchievementId("emociones_30_dias"),
            name        = "Un tibio emocional",
            description = "Registra tus emociones por 30 días consecutivos."
        ),
        Achievement(
            id          = AchievementId("noti_personalizada"),
            name        = "¡Ya es hora!",
            description = "Descubriste la personalización de notificaciones desde configuración."
        )
    )
}
