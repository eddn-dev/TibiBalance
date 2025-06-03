package com.app.domain.enums

import kotlinx.serialization.Serializable

/**
 * Define los posibles estados de una `HabitActivity`.
 */
@Serializable
enum class ActivityStatus {
    /** La actividad está programada pero aún no es momento de registrarla o no se ha interactuado. */
    PENDING,

    /** La actividad está dentro del periodo válido para que el usuario registre su progreso (generalmente, el mismo día de `activityDate`). */
    AVAILABLE_FOR_LOGGING,

    /** El usuario completó la actividad según el objetivo (`recordedQty` >= `targetQty`, o marcada como hecha si `targetQty` es `null`). */
    COMPLETED,

    /** El usuario registró un progreso, pero este es menor que el `targetQty` (y mayor que cero). */
    PARTIALLY_COMPLETED,

    /** El tiempo para registrar la actividad expiró y no hubo registro, o el usuario indicó explícitamente que no se realizó. */
    MISSED
}