/* ui/components/inputs/InputEmail.kt */
package com.app.tibibalance.ui.components.inputs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun InputEmail(
    value          : String,
    onValueChange  : (String) -> Unit,
    modifier       : Modifier = Modifier,
    label          : String   = "Correo electrónico",
    isError        : Boolean  = false,
    supportingText : String?  = null,
    maxChars       : Int?     = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction    = ImeAction.Done
    ),
    readOnly       : Boolean  = false,
    enabled        : Boolean  = true
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor   = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor  = MaterialTheme.colorScheme.surface,
        focusedBorderColor      = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        unfocusedBorderColor    = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.outline,
        errorBorderColor        = MaterialTheme.colorScheme.error,
        focusedLabelColor       = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        unfocusedLabelColor     = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        errorLabelColor         = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error
    )

    OutlinedTextField(
        value          = value,
        onValueChange  = { newValue ->
            val finalVal = if (maxChars != null) newValue.take(maxChars) else newValue
            onValueChange(finalVal)
        },
        modifier       = modifier.fillMaxWidth(),
        label          = { Text(label) },
        singleLine     = true,
        isError        = isError,
        keyboardOptions = keyboardOptions,
        supportingText = {
            when {
                isError && supportingText != null -> Text(supportingText)
                maxChars != null                 -> Text("${value.length}/$maxChars")
            }
        },
        shape   = RoundedCornerShape(12.dp),
        colors  = colors,
        readOnly = readOnly,
        enabled  = enabled
    )
}
