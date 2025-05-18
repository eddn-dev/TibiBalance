package com.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.mappers.toAuthError
import com.app.data.mappers.toFirestoreMap
import com.app.domain.common.SyncMeta
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.app.domain.error.AuthError
import com.app.domain.error.AuthResult
import com.app.domain.model.UserCredentials
import com.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    /* ── Estado de sesión ───────────────────────────────────────── */
    override fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a -> trySend(a.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun signUp(
        credentials: UserCredentials,
        displayName: String,
        birthDate  : LocalDate
    ): AuthResult<Unit> = try {
        auth.createUserWithEmailAndPassword(credentials.email, credentials.password).await()
        auth.currentUser?.sendEmailVerification()?.await()
        auth.currentUser?.let { firestore.ensureUserDocument(it, displayName, birthDate) }
        AuthResult.Success(Unit)
    } catch (t: Throwable) { AuthResult.Error(t.toAuthError()) }

    /* ── Inicio de sesión e-mail / pass ────────────────────────── */
    override suspend fun signIn(credentials: UserCredentials): AuthResult<Boolean> =
        try {
            auth.signInWithEmailAndPassword(credentials.email, credentials.password).await()
            AuthResult.Success(auth.currentUser?.isEmailVerified == true)
        } catch (t: Throwable) {
            AuthResult.Error(t.toAuthError())
        }

    /* ── Google One-Tap ─────────────────────────────────────────── */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun signInWithGoogle(idToken: String): AuthResult<Boolean> = try {
        val cred = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(cred).await()

        // user nunca es null tras cred exitosa
        auth.currentUser?.let { firestore.ensureUserDocument(it) }

        AuthResult.Success(true)                     // Google siempre verificado
    } catch (t: Throwable) {
        AuthResult.Error(t.toAuthError())
    }


    /* ── Restablecer contraseña ────────────────────────────────── */
    override suspend fun sendPasswordReset(email: String): AuthResult<Unit> =
        try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (t: Throwable) {
            AuthResult.Error(t.toAuthError())
        }

    /* ── Verificación directa ──────────────────────────────────── */
    override suspend fun isEmailVerified(): Boolean =
        auth.currentUser?.isEmailVerified == true    // single-source-of-truth

    override suspend fun sendEmailVerification(): AuthResult<Unit> = try {
        auth.currentUser?.sendEmailVerification()?.await()
        AuthResult.Success(Unit)
    } catch (t: Throwable) { AuthResult.Error(t.toAuthError()) }

    override suspend fun reload(): AuthResult<Unit> = try {
        auth.currentUser?.reload()?.await()
        AuthResult.Success(Unit)
    } catch (t: Throwable) { AuthResult.Error(t.toAuthError()) }


    override suspend fun signOut() = auth.signOut()

    /* ── Sincroniza bandera emailVerified en Firestore ─────────── */
    override suspend fun syncVerification(): AuthResult<Unit> {
        return try {
            val user = auth.currentUser
                ?: return AuthResult.Error(AuthError.Unknown(IllegalStateException("No user")))
            if (!user.isEmailVerified) return AuthResult.Success(Unit)

            firestore.collection("users")
                .document(user.uid)
                .update("emailVerified", true)
                .await()
            AuthResult.Success(Unit)
        } catch (t: Throwable) {
            AuthResult.Error(t.toAuthError())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun FirebaseFirestore.ensureUserDocument(user: FirebaseUser) {
        val uid = user.uid
        val docRef = collection("users").document(uid)
        val snap   = docRef.get().await()
        if (snap.exists()) return                    // ya registrado ✅

        val dob = java.time.LocalDate.now().minusYears(18).toKotlinLocalDate()

        val sync = SyncMeta(
            createdAt  = Clock.System.now(),
            updatedAt  = Clock.System.now(),
            deletedAt  = null,
            pendingSync = false
        )

        val settings = UserSettings(
            theme            = ThemeMode.SYSTEM,
            notifGlobal      = true,
            language         = "es",
            accessibilityTTS = false
        )

        val newUser = User(
            uid         = uid,
            email       = user.email ?: "",
            displayName = user.displayName,
            photoUrl    = user.photoUrl?.toString(),
            birthDate   = dob,
            settings    = settings,
            meta        = sync
        ).toFirestoreMap()

        docRef.set(newUser, SetOptions.merge()).await()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun FirebaseFirestore.ensureUserDocument(
        user        : FirebaseUser,
        displayName : String?      = null,
        birthDate   : LocalDate?   = null
    ) {
        val doc = collection("users").document(user.uid)
        if (doc.get().await().exists()) return            // ya existe ⇒ nada que hacer

        /* Usa los valores recibidos; si vienen nulos aplica fallback */
        val dob   = birthDate ?: java.time.LocalDate.now().minusYears(18)
            .toKotlinLocalDate()
        val name  = displayName ?: user.displayName

        val meta = SyncMeta(
            createdAt  = Clock.System.now(),
            updatedAt  = Clock.System.now(),
            deletedAt  = null,
            pendingSync = false
        )
        val settings = UserSettings(
            theme            = ThemeMode.SYSTEM,
            notifGlobal      = true,
            language         = "es",
            accessibilityTTS = false
        )
        val newUser = User(
            uid         = user.uid,
            email       = user.email ?: "",
            displayName = name,
            photoUrl    = user.photoUrl?.toString(),
            birthDate   = dob,
            settings    = settings,
            meta        = meta
        ).toFirestoreMap()

        doc.set(newUser, SetOptions.merge()).await()
    }
}
