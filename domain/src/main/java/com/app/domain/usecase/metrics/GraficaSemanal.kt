package com.app.domain.usecase.metrics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Punto para UI: transforma los 7 valores en una lista ordenada asc
 * con etiquetas legibles (dd MMM) y valores paso-kcal.
 */
class GraficaSemanal @Inject constructor(
    private val observeLast7: ObserveLast7Days
) {
    data class Entry(
        val label : String,      // "12 Jun"
        val steps : Int,
        val kcal  : Int
    )

    operator fun invoke(): Flow<List<Entry>> =
        observeLast7().map { list ->
            list.sortedBy { it.date }.map { d ->
                Entry(
                    label = d.date.toString(),   // formatea a tu gusto
                    steps = d.steps,
                    kcal  = d.kcal
                )
            }
        }
}
