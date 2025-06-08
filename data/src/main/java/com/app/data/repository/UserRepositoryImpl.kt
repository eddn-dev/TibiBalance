/* :data/repository/UserRepositoryImpl.kt */
package com.app.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.app.data.local.dao.UserDao
import com.app.data.mappers.UserMappers.toDomain
import com.app.data.mappers.UserMappers.toEntity
import com.app.data.mappers.toFirestoreDate
import com.app.data.mappers.toFirestoreMap
import com.app.data.mappers.toTimestamp
import com.app.data.mappers.toUser
import com.app.domain.common.SyncMeta
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.enums.ThemeMode
import com.app.domain.repository.UserRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val db : FirebaseFirestore,
    private val dao: UserDao,
    @IoDispatcher private val io: CoroutineDispatcher
) : UserRepository {

    /* ───── lectura reactiva ───── */
    override fun observe(uid: String): Flow<User> =
        dao.observe(uid).map { it?.toDomain() ?: UserPlaceholder(uid) }

    /* ───── updateProfile ──────── */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateProfile(
        uid: String,
        displayName: String?,
        birthDate: LocalDate?,
        photoUrl: String?
    ): Result<Unit> = withContext(io) {
        runCatching {
            val now = Clock.System.now()

            /* 1️⃣  PUSH remoto */
            val payloadForRemote = mutableMapOf<String, Any>()

            displayName?.let { payloadForRemote["displayName"] = it }
            birthDate?.let { payloadForRemote["birthDate"] = it.toFirestoreDate() } // Date
            photoUrl?.let { payloadForRemote["photoUrl"] = it }

            // Always set/update the meta field to be a map with updatedAt
            payloadForRemote["meta"] = mapOf("updatedAt" to now.toTimestamp())

            db.collection("users").document(uid)
                .set(payloadForRemote, SetOptions.merge()).await()

            /* 2️⃣  Actualiza local */
            val local = dao.find(uid)
            val merged = local?.toDomain()?.copy(
                displayName = displayName ?: local.displayName,
                birthDate = birthDate ?: local.birthDate,
                photoUrl = photoUrl ?: local.photoUrl,
                meta = local.meta.copy(
                    updatedAt = now,
                    pendingSync = false
                )
            ) ?: return@runCatching

            dao.upsert(merged.toEntity())
        }
    }

    /* ───── updateSettings ─────── */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateSettings(uid: String, s: UserSettings): Result<Unit> =
        withContext(io) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()

                /* Firestore (estructura anidada) */
                db.collection("users").document(uid)
                    .update("settings", s).await()

                /* Room */
                dao.updateSettings(
                    uid            = uid,
                    theme          = s.theme.name,
                    notifGlobal    = s.notifGlobal,
                    notifEmotion   = s.notifEmotion,
                    emotionTime    = s.notifEmotionTime,   // ← NUEVO
                    lang           = s.language,
                    tts            = s.accessibilityTTS,
                    updatedAt      = now
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncNow(uid: String): Result<Unit> = withContext(io) {
        try {
            Log.d("UserSync", "► start  uid=$uid")

            /* 1️⃣  PUSH locales pendientes ----------------------------------- */
            val pendings = dao.pendingToSync(uid)
            Log.d("UserSync", "  pendingToSync=${pendings.size}")

            pendings.forEach { local ->
                Log.d("UserSync", "  PUSH  → Firestore  updatedAt=${local.meta.updatedAt}")
                db.collection("users").document(uid)
                    .set(local.toDomain().toFirestoreMap(), SetOptions.merge())
                    .await()
            }
            if (pendings.isNotEmpty()) dao.clearPending(uid)

            /* 2️⃣  PULL remoto y LWW ---------------------------------------- */
            val snap   = db.collection("users").document(uid).get().await()
            val remote = snap.toUser()                          // null si no existe
            val local  = dao.find(uid)?.toDomain()              // null si no existe

            Log.d("UserSync", "  PULL  ← remote.updated=${remote?.meta?.updatedAt}")
            Log.d("UserSync", "         local .updated=${local ?.meta?.updatedAt}")

            val winner = when {
                local == null   -> remote
                remote == null  -> local
                local.meta.updatedAt >= remote.meta.updatedAt -> local
                else            -> remote
            }

            winner?.let {
                Log.d("UserSync", "  LWW   winner.updated=${it.meta.updatedAt}")
                dao.upsert(it.toEntity())
            }

            Log.d("UserSync", "► end")
            Result.success(Unit)                // 👈  Unit no-nulo ⇒ Result<Unit>
        } catch (ex: Exception) {
            Log.e("UserSync", "✖ error", ex)
            Result.failure(ex)
        }
    }

    /* ---- placeholder si no hay registro local (no debería ocurrir) ---- */
    private fun UserPlaceholder(uid: String) = User(
        uid       = uid,
        email     = "",
        birthDate = LocalDate(2000,1,1),
        meta      = SyncMeta()
    )
}
