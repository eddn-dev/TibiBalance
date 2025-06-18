package com.app.domain.entities

import kotlinx.datetime.Instant

/**
 * IMMUtable snapshot shown in Home-Dashboard.
 *
 * @property stepsToday   total pasos desde 00:00 del día local hasta “ahora”.
 * @property kcalToday    calorías activas quemadas en el mismo rango.
 * @property heartRate    último BPM disponible o null si no hay lectura.
 * @property hrAgeMillis  milisegundos transcurridos desde la última lectura FC;
 *                        facilita formatear (“hace 33 s”) en la UI.
 */
data class DashboardSnapshot(
    val stepsToday   : Int,
    val kcalToday    : Int,
    val heartRate    : Int?,
    val hrAgeMillis  : Long          // derivado en repo con Clock.System.now()
)
