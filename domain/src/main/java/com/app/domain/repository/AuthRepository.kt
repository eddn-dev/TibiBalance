package com.app.domain.repository

import com.app.domain.model.UserCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de autenticación para Firebase.
 */
interface AuthRepository {

    fun authState(): Flow<String?>

    suspend fun signUp(credentials: UserCredentials): Result<String>
    suspend fun signIn(credentials: UserCredentials): Result<String>
    suspend fun signInWithGoogle(idToken: String): Result<String>
    suspend fun sendPasswordReset(email: String): Result<Void?>
    suspend fun isEmailVerified(): Boolean
    suspend fun signOut()

    /**  ⇢  NUEVO: sincroniza flag de email verificado en Firestore  */
    suspend fun syncVerification(): Result<Unit>
}
