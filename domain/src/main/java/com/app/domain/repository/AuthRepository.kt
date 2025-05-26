package com.app.domain.repository

import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Contrato de autenticación para Firebase.
 */
interface AuthRepository {
    fun authState(): Flow<String?>

    suspend fun signUp(
        credentials : UserCredentials,
        displayName : String,
        birthDate   : LocalDate
    ): AuthResult<Unit>

    suspend fun signIn(credentials: UserCredentials): AuthResult<Boolean>
    suspend fun signInWithGoogle(idToken: String): AuthResult<Boolean>
    suspend fun sendPasswordReset(email: String): AuthResult<Unit>
    suspend fun isEmailVerified(): Boolean        // sigue devolviendo Bool puro
    suspend fun signOut()
    suspend fun sendEmailVerification(): AuthResult<Unit>
    suspend fun reload(): AuthResult<Unit>
    suspend fun syncVerification(): AuthResult<Unit>
    fun currentProvider(): String?
}

