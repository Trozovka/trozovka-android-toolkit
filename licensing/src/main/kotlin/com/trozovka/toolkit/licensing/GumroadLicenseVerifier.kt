package com.trozovka.toolkit.licensing

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Calls Gumroad's public license-verification API. Blocking I/O -- run on Dispatchers.IO. */
object GumroadLicenseVerifier {

    sealed class Result {
        data class Valid(val purchaseJson: String) : Result()
        object Invalid : Result()
        data class NetworkError(val message: String) : Result()
    }

    private const val ENDPOINT = "https://api.gumroad.com/v2/licenses/verify"
    private const val TIMEOUT_MILLIS = 10_000

    /**
     * [productId] is the Gumroad product ID shown in the product's License key block, not its
     * permalink: Gumroad rejects permalink-only requests for newer products.
     */
    fun verify(productId: String, licenseKey: String): Result {
        if (productId.isBlank() || licenseKey.isBlank()) return Result.Invalid

        return try {
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
            }

            OutputStreamWriter(connection.outputStream).use { it.write(requestBody(productId, licenseKey)) }

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            if (json.optBoolean("success", false)) {
                Result.Valid(json.optJSONObject("purchase")?.toString() ?: "")
            } else {
                Result.Invalid
            }
        } catch (e: Exception) {
            Result.NetworkError(e.message ?: e.javaClass.simpleName)
        }
    }

    internal fun requestBody(productId: String, licenseKey: String): String =
        "product_id=${URLEncoder.encode(productId, "UTF-8")}" +
            "&license_key=${URLEncoder.encode(licenseKey, "UTF-8")}"
}
