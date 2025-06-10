// :app/auth/GoogleOneTapHelper.kt
package com.app.tibibalance.auth

import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

object GoogleOneTapHelper {

    /**
     * Crea la petición de Credential Manager para Google One Tap.
     *
     * @param serverClientId  Client ID de OAuth 2.0 asociado al proyecto.
     * @param authorizedOnly   Si es `true`, se muestran únicamente las cuentas
     *                         que ya se hayan autorizado previamente en la app.
     */
    fun buildRequest(
        serverClientId: String,
        authorizedOnly: Boolean = false,
    ): GetCredentialRequest =
        GetCredentialRequest(
            listOf(
                GetGoogleIdOption.Builder()
                    .setServerClientId(serverClientId)
                    .setFilterByAuthorizedAccounts(authorizedOnly)
                    .build(),
            ),
        )
}
