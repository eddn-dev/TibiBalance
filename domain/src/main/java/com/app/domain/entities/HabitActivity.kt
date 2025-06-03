package com.app.domain.entities

import com.app.domain.common.SyncMeta
import com.app.domain.enums.ActivityStatus
import com.app.domain.ids.ActivityId
import com.app.domain.ids.HabitId
import com.app.domain.enums.SessionUnit // Asegúrate que esta enum esté accesible
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Representa una instancia específica de un hábito programado que el usuario debe realizar y cuyo progreso puede ser registrado.
 * Se crea una instancia de HabitActivity por cada horario definido en NotifConfig.times para los días
 * en que el hábito, según su patrón de repetición (Habit.repeat), esté activo y sea un desafío (Habit.challenge != null).
 */
@Serializable
data class HabitActivity(
    /** Identificador único de esta instancia de actividad. */
    val id: ActivityId,

    /** ID del hábito padre al que pertenece esta actividad. */
    val habitId: HabitId,

    /** Fecha en la que esta actividad específica está programada para realizarse. */
    val activityDate: LocalDate,

    /**
     * Hora específica del día en que esta actividad está programada.
     * Proviene de uno de los `LocalTime` en `Habit.notifConfig.times`.
     * Es crucial porque un hábito puede tener múltiples ocurrencias en un mismo día.
     */
    val scheduledTime: LocalTime,

    /** Estado actual del progreso de esta actividad. Mutable. */
    var status: ActivityStatus = ActivityStatus.PENDING,

    /**
     * La cantidad objetivo para esta actividad, copiada de `Habit.session.qty`
     * en el momento en que esta `HabitActivity` fue generada.
     * Es `null` si el hábito es de tipo "marcar como hecho/no hecho" (sin cantidad).
     * Inmutable para esta instancia de actividad, para preservar el objetivo original
     * incluso si la configuración del hábito cambia posteriormente.
     */
    val targetQty: Int? = null,

    /**
     * La cantidad que el usuario ha registrado como completada para esta actividad.
     * Mutable. Es `null` si aún no se ha registrado nada o si el hábito no tiene cantidad.
     */
    var recordedQty: Int? = null,

    /**
     * La unidad de medida para `targetQty` y `recordedQty`, copiada de `Habit.session.unit`
     * en el momento en que esta `HabitActivity` fue generada.
     * Es `null` si `targetQty` es `null`.
     * Inmutable para esta instancia de actividad.
     */
    val sessionUnit: SessionUnit? = null,

    /**
     * Timestamp exacto en que la actividad fue marcada como `COMPLETED` por primera vez,
     * o el último momento en que se registró un progreso que llevó a `PARTIALLY_COMPLETED` o `COMPLETED`.
     * Mutable. `null` si no se ha completado o progresado.
     */
    var loggedAt: Instant? = null,

    /**
     * Timestamp exacto de la creación de esta instancia de actividad en el sistema.
     * Útil para auditoría y para entender cuándo se programó originalmente.
     * Diferente de `meta.createdAt` que se refiere a la creación del registro en la BBDD.
     * Este campo es más semántico del dominio.
     */
    val generatedAt: Instant = Instant.DISTANT_PAST, // Se debe establecer al crear la instancia

    /** Metadatos para la sincronización y auditoría del registro. */
    val meta: SyncMeta = SyncMeta()
)