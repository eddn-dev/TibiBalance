package com.app.data.repository

import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore          // ← INYECTADO
) : AuthRepository {

    /* ------------- estado de sesión ------------- */
    override fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a -> trySend(a.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /* ------------- e-mail / pass ------------- */
    override suspend fun signUp(credentials: UserCredentials) = runCatching {
        auth.createUserWithEmailAndPassword(credentials.email, credentials.password).await()
        auth.currentUser?.uid ?: error("uid null")
    }

    override suspend fun signIn(credentials: UserCredentials) = runCatching {
        auth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
        auth.currentUser?.uid ?: error("uid null")
    }

    /* ------------- Google ------------- */
    override suspend fun signInWithGoogle(idToken: String) = runCatching {
        val cred = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(cred).await()
        auth.currentUser?.uid ?: error("uid null")
    }

    /* ------------- utilidades ------------- */
    override suspend fun sendPasswordReset(email: String) =
        runCatching { auth.sendPasswordResetEmail(email).await() }

    override suspend fun isEmailVerified(): Boolean =
        auth.currentUser?.isEmailVerified == true            // single source of truth


    override suspend fun signOut() { auth.signOut() }

    /* ------------- ⇢ NUEVO: syncVerification ------------- */
    override suspend fun syncVerification(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("No user"))
        if (!user.isEmailVerified) return Result.success(Unit)   // nada que hacer

        firestore.collection("users")
            .document(user.uid)
            .update("emailVerified", true)                      // campo que prefieras
            .await()
    }
}
