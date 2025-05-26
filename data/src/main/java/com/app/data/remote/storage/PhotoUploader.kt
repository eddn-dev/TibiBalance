/* data/remote/PhotoUploader.kt */
package com.app.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoUploader @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    /**
     * Lee la imagen del [uri] y la codifica como
     * `data:image/jpeg;base64,.....` (ó png si detectas otra extensión).
     * No sube nada a Storage; simplemente devuelve la cadena lista para
     * guardarse en Firestore.
     */
    suspend fun upload(uid: String, uri: Uri): String = withContext(Dispatchers.IO) {
        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen")

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    }
}
