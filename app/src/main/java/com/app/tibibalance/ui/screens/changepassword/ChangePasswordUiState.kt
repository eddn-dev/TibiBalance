/* ui/screens/changepassword/ChangePasswordUiState.kt */
package com.app.tibibalance.ui.screens.changepassword

data class ChangePasswordUiState(
    val current       : String = "",
    val newPass       : String = "",
    val confirm       : String = "",

    val strengthError : String? = null,    // salida del PasswordValidator
    val mismatchError : String? = null,    // “no coinciden”

    val isLoading     : Boolean = false,
    val success       : Boolean = false,
    val error         : String? = null
)
