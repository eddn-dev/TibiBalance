/**
 * @file    JsonConfig.kt
 * @ingroup data_mapper
 * @brief   Configuración central de kotlinx.serialization para los mappers.
 */
package com.app.data.mappers

import kotlinx.serialization.json.Json

/**
 * Objeto singleton que estandariza la instancia de [Json] usada por todos
 * los mappers, garantizando:
 *
 *  * `encodeDefaults = true` → incluye valores por defecto en el JSON.
 *  * `ignoreUnknownKeys = true` → tolera claves extra al deserializar.
 *  * Discriminador de clases selladas = `_type` (compatibilidad Firestore).
 */

object JsonConfig {
    val default: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "_type"
    }
}
