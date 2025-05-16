package com.app.domain.repository

import com.app.domain.model.UserCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de autenticación para Firebase.
 * Todas las operaciones devuelven [Result] para evitar excepciones crudas.
 */
interface AuthRepository {

    /** Flujo del UID actual o `null` si no hay sesión. */
    fun authState(): Flow<String?>

    /** Crea una cuenta nueva con correo y contraseña. */
    suspend fun signUp(credentials: UserCredentials): Result<String>

    /** Inicia sesión con correo y contraseña. */
    suspend fun signIn(credentials: UserCredentials): Result<String>

    /** Inicia sesión con Google usando el ID token proporcionado. */
    suspend fun signInWithGoogle(idToken: String): Result<String>

    /** Envía un correo para restablecer la contraseña. */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /** Cierra la sesión actual. */
    suspend fun signOut()
}