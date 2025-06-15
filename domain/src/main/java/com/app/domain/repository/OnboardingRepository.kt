/**
 * @file    OnboardingRepository.kt
 * @ingroup domain_repository
 * @brief   Contrato del repositorio de Onboarding (offline-first).
 */
package com.app.domain.repository

import com.app.domain.entities.OnboardingStatus
import kotlinx.coroutines.flow.Flow

/** Administra el estado de onboarding para el usuario autenticado. */
interface OnboardingRepository {

    /**
     * Observa en tiempo real el progreso de onboarding del `uid` dado.
     * @param uid Identificador único del usuario.
     * @return    Flujo reactivo con el último [OnboardingStatus].
     */
    fun observe(uid: String): Flow<OnboardingStatus>

    /**
     * Persiste un nuevo [OnboardingStatus] para el usuario.
     * Debe disparar la sincronización remota en la capa de datos.
     */
    suspend fun save(uid: String, status: OnboardingStatus)
    suspend fun saveTutorialStatus(uid: String, completed: Boolean)
    suspend fun syncNow(uid: String): Result<Unit>

    suspend fun getStatus(uid: String): OnboardingStatus
    suspend fun saveStatus(uid: String, status: OnboardingStatus)
}
