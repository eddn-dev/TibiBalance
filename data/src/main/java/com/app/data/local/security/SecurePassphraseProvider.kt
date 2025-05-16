package com.app.data.local.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Proveedor de SupportFactory cifrada mediante Android Keystore.
 */
@Singleton
class SecurePassphraseProvider @Inject constructor(
    @ApplicationContext private val ctx: Context
) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()                                       // genera/recupera clave AES GCM 256 bits :contentReference[oaicite:1]{index=1}

        EncryptedSharedPreferences.create(
            ctx,
            "sqlcipher_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )                                                 // datos cifrados a nivel de archivo :contentReference[oaicite:2]{index=2}
    }

    private fun generateRandomPassphrase(): ByteArray =
        ByteArray(32).also { SecureRandom().nextBytes(it) }  // 256 bit key :contentReference[oaicite:3]{index=3}

    /** Devuelve SupportFactory con passphrase almacenada en Keystore. */
    fun provideFactory(): SupportFactory {
        val stored = prefs.getString("pp", null)
        val pass: ByteArray =
            if (stored != null) stored.decodeHex()
            else generateRandomPassphrase().also {
                prefs.edit().putString("pp", it.encodeHex()).apply()
            }

        // SQLCipher borrará la clave de memoria tras abrir la BD (clearPassphrase=true) :contentReference[oaicite:4]{index=4}
        return SupportFactory(pass, null, true)
    }

    /* --- helpers hex --- */
    private fun ByteArray.encodeHex(): String =
        joinToString("") { "%02x".format(it) }
    private fun String.decodeHex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
