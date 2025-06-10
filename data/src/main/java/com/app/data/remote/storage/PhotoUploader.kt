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
        val inputStream = ctx.contentResolver.openInputStream(uri)
            ?: error("No se pudo abrir la imagen")

        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            ?: error("No se pudo decodificar la imagen")

        val outputStream = java.io.ByteArrayOutputStream()

        // Comprime en JPEG al 80% de calidad (ajustable)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)

        val compressedBytes = outputStream.toByteArray()

        val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$base64"
    }

}
