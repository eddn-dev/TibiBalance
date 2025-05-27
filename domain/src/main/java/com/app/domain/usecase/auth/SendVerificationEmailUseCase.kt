package com.app.domain.usecase.auth

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class SendVerificationEmailUseCase @Inject constructor() {
    suspend operator fun invoke(email: String): Boolean = withContext(Dispatchers.IO) {
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
            connection.disconnect()

            responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
