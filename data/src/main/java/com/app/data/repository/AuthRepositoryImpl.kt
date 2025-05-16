package com.app.data.repository

import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(credentials: UserCredentials): Result<String> =
        runCatching {
            auth.createUserWithEmailAndPassword(credentials.email, credentials.password).await()
            auth.currentUser?.uid ?: throw IllegalStateException("uid null")
        }

    override suspend fun signIn(credentials: UserCredentials): Result<String> =
        runCatching {
            auth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
            auth.currentUser?.uid ?: throw IllegalStateException("uid null")
        }

    override suspend fun signInWithGoogle(idToken: String): Result<String> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            auth.currentUser?.uid ?: throw IllegalStateException("uid null")
        }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching { auth.sendPasswordResetEmail(email).await() }

    override suspend fun signOut() {
        auth.signOut()
    }
}