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
        val PREF_NAME = "sqlcipher_prefs"
        val MASTER_ALIAS = "tibibalance_master_key"   // ¡no lo cambies entre builds!

        /* 1️⃣ Crea (o recupera) la master key */
        fun newMasterKey(): MasterKey = MasterKey.Builder(ctx, MASTER_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        /* 2️⃣ Crea los EncryptedSharedPreferences con esa key */
        fun createPrefs(masterKey: MasterKey) = EncryptedSharedPreferences.create(
            ctx,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        try {
            /* Primer intento: usamos el keyset existente */
            createPrefs(newMasterKey())
        } catch (ex: javax.crypto.AEADBadTagException) {
            /* Keyset corrupto → lo borramos y regeneramos */
            ctx.deleteSharedPreferences(PREF_NAME)

            // (Opcional) también elimina la master key si quieres renovar todo
            runCatching {
                val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                ks.deleteEntry(MASTER_ALIAS)
            }

            // Segundo intento: prefs “limpios” con una master key nueva
            createPrefs(newMasterKey())
        }
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
