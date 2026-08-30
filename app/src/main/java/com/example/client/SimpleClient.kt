package com.example.client

import android.util.Log
import com.example.models.JsonUtils
import com.example.models.Message
import com.example.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object SimpleClient {
    private const val TAG = "SimpleClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .writeTimeout(4, TimeUnit.SECONDS)
        .build()

    const val PORT = 8080

    fun cleanHost(rawIp: String): String {
        var clean = rawIp.trim()
        // Convert Persian & Arabic numbers to ASCII
        clean = clean.map { char ->
            when (char) {
                '۰', '٠' -> '0'
                '۱', '١' -> '1'
                '۲', '٢' -> '2'
                '۳', '٣' -> '3'
                '۴', '٤' -> '4'
                '۵', '٥' -> '5'
                '۶', '٦' -> '6'
                '۷', '٧' -> '7'
                '۸', '٨' -> '8'
                '۹', '٩' -> '9'
                else -> char
            }
        }.joinToString("")

        // Remove http:// or https:// prefix
        if (clean.startsWith("http://", ignoreCase = true)) {
            clean = clean.substring(7)
        } else if (clean.startsWith("https://", ignoreCase = true)) {
            clean = clean.substring(8)
        }

        // Remove trailing slash
        if (clean.endsWith("/")) {
            clean = clean.dropLast(1)
        }

        // Strip port if user already included :8080
        if (clean.contains(":")) {
            clean = clean.substringBefore(":")
        }

        // Keep only valid host characters
        clean = clean.filter { it.isLetterOrDigit() || it == '.' || it == '-' }

        return if (clean.isEmpty()) "127.0.0.1" else clean
    }

    fun getBaseUrl(ip: String): String? {
        val host = cleanHost(ip)
        return try {
            val httpUrl = HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .port(PORT)
                .build()
            httpUrl.toString().removeSuffix("/")
        } catch (e: Exception) {
            Log.d(TAG, "Invalid host format: '$ip' (cleaned: '$host')")
            null
        }
    }

    suspend fun ping(serverIp: String): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(serverIp) ?: return@withContext false
        try {
            val url = "$baseUrl/ping"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.d(TAG, "Ping unsuccessful for: $baseUrl (${e.message})")
            false
        }
    }

    suspend fun registerUser(serverIp: String, name: String): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(serverIp) ?: return@withContext false
        try {
            val encodedName = URLEncoder.encode(name.trim(), "UTF-8")
            val url = "$baseUrl/register?name=$encodedName"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    bodyStr.contains("\"success\":true")
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Registration failed: ${e.message}")
            false
        }
    }

    suspend fun getUsers(serverIp: String): List<User> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(serverIp) ?: return@withContext emptyList()
        try {
            val url = "$baseUrl/users"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: "[]"
                    JsonUtils.parseUsersJson(bodyStr)
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Get users failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMessages(serverIp: String, user: String): List<Message> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(serverIp) ?: return@withContext emptyList()
        try {
            val encodedUser = URLEncoder.encode(user.trim(), "UTF-8")
            val url = "$baseUrl/messages?user=$encodedUser"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: "[]"
                    JsonUtils.parseMessagesJson(bodyStr)
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Get messages failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(serverIp: String, from: String, to: String, text: String): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(serverIp) ?: return@withContext false
        try {
            val encodedFrom = URLEncoder.encode(from.trim(), "UTF-8")
            val encodedTo = URLEncoder.encode(to.trim(), "UTF-8")
            val encodedText = URLEncoder.encode(text.trim(), "UTF-8")
            val url = "$baseUrl/send?from=$encodedFrom&to=$encodedTo&text=$encodedText"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    bodyStr.contains("\"success\":true")
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Send message failed: ${e.message}")
            false
        }
    }
}
