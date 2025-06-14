package com.app.domain.usecase.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

sealed class ResendEmailResult {
    data object Success : ResendEmailResult()
    data class Failure(val reason: String) : ResendEmailResult()
}

class ResendVerificationUseCase @Inject constructor() {
    suspend operator fun invoke(email: String): ResendEmailResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://tibiserver.onrender.com/send-confirmation")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val json = JSONObject().apply {
                put("email", email)
            }

            connection.outputStream.use {
                it.write(json.toString().toByteArray())
            }

            val responseCode = connection.responseCode
            val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            if (responseCode == 200) {
                ResendEmailResult.Success
            } else {
                ResendEmailResult.Failure("HTTP $responseCode: $responseMessage")
            }
        } catch (e: Exception) {
            ResendEmailResult.Failure(e.localizedMessage ?: "Error desconocido")
        }
    }
}
