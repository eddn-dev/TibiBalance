package com.app.tibibalance.ui.screens.profile

import com.app.domain.entities.User

data class EditProfileUiState(
    val loading : Boolean      = false,   // spinner global (guardar / subir foto)
    val success : Boolean      = false,   // true ⇒ mostrar Modal de éxito
    val error   : String?      = null,    // mensaje de error (null → sin error)
    val user    : User?        = null,    // perfil actual
    val photoUri: String?      = null     // preview de foto recién elegida
)
