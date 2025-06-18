package com.app.domain.usecase.metrics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Versión 30 días para gráfica mensual.
 */
class GraficaMensualUseCase @Inject constructor(
    private val observeLast30: ObserveLast30Days
) {
    data class Entry(
        val label : String,
        val steps : Int,
        val kcal  : Int
    )

    operator fun invoke(): Flow<List<Entry>> =
        observeLast30().map { list ->
            list.sortedBy { it.date }.map { d ->
                Entry(
                    label = d.date.dayOfMonth.toString(), // "1", "2", …
                    steps = d.steps,
                    kcal  = d.kcal
                )
            }
        }
}
