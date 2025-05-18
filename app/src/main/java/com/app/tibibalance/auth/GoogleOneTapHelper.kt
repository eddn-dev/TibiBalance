// :app/auth/GoogleOneTapHelper.kt
package com.app.tibibalance.auth

import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

object GoogleOneTapHelper {

    /** Crea la petición de Credential-Manager con el client-ID web de OAuth 2. */
    fun buildRequest(serverClientId: String): GetCredentialRequest =
        GetCredentialRequest(
            listOf(
                GetGoogleIdOption.Builder()
                    .setServerClientId(serverClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
        )
}
