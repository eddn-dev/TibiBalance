/**
 * @file      TibiBalanceApp.kt
 * @ingroup   app_bootstrap
 * @brief     Punto de entrada de la aplicación.  Inicializa Hilt y configura
 *            servicios globales (p.-ej., Firestore logging).
 *
 * @details
 *  - Anotamos con **@HiltAndroidApp** para que Dagger-Hilt genere el grafo.
 *  - Activamos el log VERBOSE de Firestore para facilitar depuración en Logcat.
 */
package com.app.tibibalance   // ¡mantén el mismo package en todo el módulo!

