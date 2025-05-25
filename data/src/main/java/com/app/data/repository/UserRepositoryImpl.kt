/* :data/repository/UserRepositoryImpl.kt */
package com.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.data.local.dao.UserDao
import com.app.data.mappers.UserMappers.toDomain
import com.app.data.mappers.UserMappers.toEntity
import com.app.domain.common.SyncMeta
import com.app.domain.entities.User
import com.app.domain.entities.UserSettings
import com.app.domain.repository.UserRepository
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
            val payload = buildMap<String, Any> {
                put("meta.updatedAt", now.toString())
                displayName?.let { put("displayName", it) }
                birthDate  ?.let { put("birthDate",  it.toString()) }
                photoUrl   ?.let { put("photoUrl",   it) }
            }
            if (payload.size > 1) {                        // hay algo distinto a updatedAt
                db.collection("users").document(uid)
                    .set(payload, SetOptions.merge()).await()
            }

            /* 2️⃣  Actualiza local */
            val local = dao.find(uid)
            val merged = local?.toDomain()?.copy(
                displayName = displayName ?: local.displayName,
                birthDate   = birthDate   ?: local.birthDate,
                photoUrl    = photoUrl    ?: local.photoUrl,
                meta        = local.meta.copy(
                    updatedAt   = now,
                    pendingSync = false
                )
            ) ?: return@runCatching

            dao.upsert(merged.toEntity())
        }
    }

    /* ───── updateSettings ─────── */
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateSettings(uid: String, settings: UserSettings): Result<Unit> =
        withContext(io) {
            runCatching {
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())

                /* Firestore */
                db.collection("users").document(uid)
                    .update("settings", settings).await()

                /* Room */
                dao.updateSettings(
                    uid,
                    settings.theme.name,
                    settings.notifGlobal,
                    settings.language,
                    settings.accessibilityTTS,
                    now.toEpochMilliseconds()
                )
            }
        }

    /* ───── syncNow offline-first ── */@RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncNow(uid: String): Result<Unit> = withContext(io) {
        runCatching {

            /* 1️⃣ PUSH locales pendientes */
            dao.pendingToSync(uid).forEach { local ->
                db.collection("users").document(uid)
                    .set(local.toDomain(), SetOptions.merge()).await()
            }
            dao.clearPending(uid)  // una sola llamada basta

            /* 2️⃣ PULL remoto y LWW */
            val snap   = db.collection("users").document(uid).get().await()
            val remote = snap.toObject(User::class.java)

            if (remote != null) {
                val local  = dao.find(uid)
                val winner = when {
                    local == null -> remote
                    local.meta.updatedAt >= remote.meta.updatedAt -> local.toDomain()
                    else -> remote
                }
                dao.upsert(winner.toEntity())
            }

            /* 👇 garantiza Unit como resultado del bloque */
            Unit
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
