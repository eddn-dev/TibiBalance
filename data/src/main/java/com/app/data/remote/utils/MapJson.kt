/* data/remote/utils/MapJson.kt */
package com.app.data.remote.utils

import kotlinx.serialization.json.*

/** Convierte un `Map<String,Any?>` (Firestore) a `JsonElement` recursivamente */
@Suppress("UNCHECKED_CAST")
fun Map<*, *>.asJson(): JsonElement = buildJsonObject {
    for ((k, v) in this@asJson) {
        val key = k as String
        put(key, v.toJson())
    }
}

private fun Any?.toJson(): JsonElement = when (this) {
    null            -> JsonNull
    is Number       -> JsonPrimitive(this)
    is Boolean      -> JsonPrimitive(this)
    is String       -> JsonPrimitive(this)
    is Map<*, *>    -> (this as Map<*, *>).asJson()
    is List<*>      -> buildJsonArray { this@toJson.forEach { add(it.toJson()) } }
    else            -> JsonPrimitive(toString())   // fallback seguro
}
