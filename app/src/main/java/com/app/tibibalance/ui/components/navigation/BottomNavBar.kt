/**
 * @file    BottomNavBar.kt
 * @ingroup ui_component_navigation // Grupo específico para componentes de navegación
 * @brief   Define un [Composable] para la barra de navegación inferior de la aplicación.
 *
 * @details Este archivo contiene el [Composable] `BottomNavBar`, que renderiza la
 * barra de navegación principal ubicada en la parte inferior de la pantalla.
 * Utiliza un [Surface] con elevación y bordes superiores redondeados para el fondo
 * y un [Row] para distribuir horizontalmente los elementos de navegación ([NavBarButton]).
 *
 * Recibe una lista de [BottomNavItem] que definen los destinos, la ruta seleccionada
 * actual, y un callback para notificar cuando se selecciona un nuevo elemento.
 *
 * @see BottomNavItem Data class que define un elemento de la barra de navegación.
 * @see NavBarButton Composable utilizado para renderizar cada elemento individual en la barra.
 * @see Surface Componente de Material 3 usado como contenedor con elevación.
 * @see Row Layout composable para organizar los botones horizontalmente.
 * @see MaterialTheme Usado para obtener el color de fondo (`surface`).
 */
package com.app.tibibalance.ui.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding // Importar si se usa en preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview // Importar Preview
import androidx.compose.ui.unit.dp
import com.app.tibibalance.ui.components.buttons.NavBarButton // Importar NavBarButton
// Importar BottomNavItem si está en otro paquete
// import com.app.tibibalance.ui.components.navigation.BottomNavItem
// Importar iconos para la preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.* // Para remember y mutableStateOf en preview
import androidx.compose.ui.platform.testTag
import com.app.tibibalance.tutorial.TutorialStepData
import com.psoffritti.taptargetcompose.TapTarget
import com.psoffritti.taptargetcompose.TextDefinition
import com.app.tibibalance.tutorial.rememberTutorialTarget
import com.app.tibibalance.tutorial.TutorialViewModel // solo si usas el viewModel aquí
import androidx.compose.ui.geometry.Rect

/**
 * @brief Un [Composable] que renderiza la barra de navegación inferior de la aplicación.
 *
 * @details Muestra una fila de botones de navegación ([NavBarButton]) dentro de un [Surface]
 * estilizado con elevación y bordes superiores redondeados. Distribuye los botones
 * uniformemente a lo largo del ancho disponible.
 *
 * @param items La [List] de [BottomNavItem] que define qué elementos (icono, etiqueta, ruta)
 * mostrar en la barra de navegación.
 * @param selectedRoute El [String] de la ruta (`BottomNavItem.route`) que corresponde al
 * destino actualmente seleccionado. Se utiliza para resaltar visualmente el botón activo.
 * Puede ser `null` si ninguna ruta está seleccionada.
 * @param onItemClick La función lambda que se invoca cuando el usuario pulsa sobre uno de los
 * [NavBarButton]. Recibe el [String] de la ruta (`BottomNavItem.route`) del elemento pulsado.
 * Esta lambda típicamente contendrá la lógica para navegar al destino correspondiente.
 * @param modifier Un [Modifier] opcional para aplicar al [Surface] contenedor principal,
 * permitiendo personalizar su posicionamiento o padding externo.
 */
@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedRoute: String?,
    onItemClick: (String) -> Unit,
    tutorialStep: TutorialStepData? = null,
    onTutorialTargetClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .navigationBarsPadding()
            .testTag("bottom_nav_bar"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarButton(
                    item = item,
                    selected = item.route == selectedRoute,
                    onClick = { onItemClick(item.route) },

                )
            }
        }
    }
}

// --- Preview ---

/**
 * @brief Previsualización Composable para [BottomNavBar].
 * @details Muestra la barra de navegación con elementos de ejemplo y simula
 * la selección de uno de ellos.
 */
@Preview(showBackground = true, name = "BottomNavBar Preview")
@Composable
private fun BottomNavBarPreview() {
    // Define una lista de elementos de ejemplo para la preview
    val sampleItems = listOf(
        BottomNavItem(label = "Inicio", route = "home", icon = Icons.Default.Home),
        BottomNavItem(label = "Perfil", route = "profile", icon = Icons.Default.Person),
        BottomNavItem(label = "Ajustes", route = "settings", icon = Icons.Default.Settings)
    )
    // Simula el estado de la ruta seleccionada usando remember y mutableStateOf
    var selectedRoute by remember { mutableStateOf(sampleItems[0].route) } // Inicia con "home" seleccionado

    // Envuelve el componente en MaterialTheme para aplicar estilos y colores
    MaterialTheme {
        // Coloca la barra en una columna con padding para verla mejor
        Column(modifier = Modifier.padding(top = 200.dp)) { // Simula estar en la parte inferior
            BottomNavBar(
                items = sampleItems, // Pasa los elementos de ejemplo
                selectedRoute = selectedRoute, // Pasa la ruta seleccionada simulada
                onItemClick = { route -> selectedRoute = route } // Actualiza el estado simulado al hacer clic
            )
        }
    }
}