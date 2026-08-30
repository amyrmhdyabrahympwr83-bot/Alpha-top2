package com.example.server

import android.util.Log
import com.example.models.JsonUtils
import com.example.models.LogEntry
import com.example.models.LogType
import com.example.models.Message
import com.example.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.UUID

object SimpleServer {
    private const val TAG = "SimpleServer"
    private const val PORT = 8080

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // State flows for real-time reactive UI update
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _registeredUsers = MutableStateFlow<List<User>>(emptyList())
    val registeredUsers: StateFlow<List<User>> = _registeredUsers

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    init {
        addLog(LogType.INFO, "سرور آماده به کار است. دکمه فعال‌سازی را فشار دهید.")
    }

    fun startServer() {
        if (_isRunning.value) return
        _isRunning.value = true
        addLog(LogType.INFO, "در حال راه‌اندازی سرور روی پورت $PORT...")

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(PORT).apply {
                    reuseAddress = true
                }
                val ip = getIpAddress()
                addLog(LogType.SUCCESS, "سرور با موفقیت فعال شد! آدرس اتصال: $ip:$PORT")

                while (true) {
                    val socket = serverSocket?.accept() ?: break
                    launch { handleClient(socket) }
                }
            } catch (e: Exception) {
                if (_isRunning.value) {
                    addLog(LogType.ERROR, "خطا در اجرای سرور: ${e.localizedMessage}")
                    Log.e(TAG, "Server error", e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isRunning.value = false
                    addLog(LogType.WARNING, "سرور غیرفعال شد.")
                }
            }
        }
    }

    fun stopServer() {
        if (!_isRunning.value) return
        _isRunning.value = false
        addLog(LogType.INFO, "در حال متوقف کردن سرور...")
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverJob?.cancel()
        serverJob = null
    }

    fun clearData() {
        _registeredUsers.value = emptyList()
        _messages.value = emptyList()
        addLog(LogType.WARNING, "تمامی اطلاعات کاربران و پیام‌ها از سرور پاک شد.")
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
            val firstLine = reader.readLine() ?: return
            
            // Expected first line format: "GET /route?query HTTP/1.1"
            val parts = firstLine.split(" ")
            if (parts.size < 2) {
                sendResponse(socket, 400, "Bad Request", """{"error":"Malformed HTTP request"}""")
                return
            }

            val method = parts[0]
            val fullPath = parts[1]

            val questionIdx = fullPath.indexOf("?")
            val path = if (questionIdx >= 0) fullPath.substring(0, questionIdx) else fullPath
            val queryString = if (questionIdx >= 0) fullPath.substring(questionIdx + 1) else null
            val params = JsonUtils.parseQueryParams(queryString)

            Log.d(TAG, "Request: $method $path, params: $params")

            when {
                path == "/ping" && method == "GET" -> {
                    sendResponse(socket, 200, "OK", """{"status":"ok","message":"Server is active"}""")
                }

                path == "/register" && method == "POST" -> {
                    val name = params["name"]?.trim()
                    if (name.isNullOrEmpty()) {
                        sendResponse(socket, 400, "Bad Request", """{"error":"Name parameter is missing or empty"}""")
                        addLog(LogType.ERROR, "درخواست ثبت‌نام نامعتبر (نام خالی بود)")
                    } else {
                        val exists = _registeredUsers.value.any { it.name.lowercase() == name.lowercase() }
                        if (!exists) {
                            val newUser = User(name)
                            _registeredUsers.value = _registeredUsers.value + newUser
                            addLog(LogType.SUCCESS, "کاربر جدید ثبت شد: «$name»")
                        } else {
                            addLog(LogType.INFO, "کاربر «$name» مجددا متصل شد.")
                        }
                        sendResponse(socket, 200, "OK", """{"success":true,"name":"${JsonUtils.escapeJson(name)}"}""")
                    }
                }

                path == "/users" && method == "GET" -> {
                    val jsonResponse = JsonUtils.serializeUsers(_registeredUsers.value)
                    sendResponse(socket, 200, "OK", jsonResponse)
                }

                path == "/send" && method == "POST" -> {
                    val from = params["from"]?.trim()
                    val to = params["to"]?.trim()
                    val text = params["text"]?.trim()

                    if (from.isNullOrEmpty() || to.isNullOrEmpty() || text.isNullOrEmpty()) {
                        sendResponse(socket, 400, "Bad Request", """{"error":"Missing parameters for sending message"}""")
                        addLog(LogType.ERROR, "خطا در مسیریابی پیام: پارامترهای ارسالی ناقص هستند.")
                    } else {
                        val newMessage = Message(
                            id = UUID.randomUUID().toString(),
                            sender = from,
                            receiver = to,
                            text = text
                        )
                        _messages.value = _messages.value + newMessage
                        addLog(LogType.MESSAGE, "پیام از «$from» به «$to»: $text")
                        sendResponse(socket, 200, "OK", """{"success":true,"id":"${newMessage.id}"}""")
                    }
                }

                path == "/messages" && method == "GET" -> {
                    val user = params["user"]?.trim()
                    if (user.isNullOrEmpty()) {
                        sendResponse(socket, 400, "Bad Request", """{"error":"Missing user parameter"}""")
                    } else {
                        val userMessages = _messages.value.filter {
                            it.sender.lowercase() == user.lowercase() || it.receiver.lowercase() == user.lowercase()
                        }
                        val jsonResponse = JsonUtils.serializeMessages(userMessages)
                        sendResponse(socket, 200, "OK", jsonResponse)
                    }
                }

                path == "/clear" && method == "POST" -> {
                    clearData()
                    sendResponse(socket, 200, "OK", """{"success":true}""")
                }

                else -> {
                    sendResponse(socket, 404, "Not Found", """{"error":"API endpoint not found"}""")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client", e)
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing socket", e)
            }
        }
    }

    private fun sendResponse(socket: Socket, statusCode: Int, statusText: String, jsonBody: String) {
        try {
            val outputStream = socket.getOutputStream()
            val bodyBytes = jsonBody.toByteArray(Charsets.UTF_8)
            val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                         "Content-Type: application/json; charset=utf-8\r\n" +
                         "Content-Length: ${bodyBytes.size}\r\n" +
                         "Access-Control-Allow-Origin: *\r\n" +
                         "Connection: close\r\n" +
                         "\r\n"
            outputStream.write(header.toByteArray(Charsets.UTF_8))
            outputStream.write(bodyBytes)
            outputStream.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending HTTP response", e)
        }
    }

    private fun addLog(type: LogType, message: String) {
        val entry = LogEntry(
            id = UUID.randomUUID().toString(),
            type = type,
            message = message
        )
        // Keep logs capped at last 100 entries for efficiency
        val currentLogs = _logs.value
        val nextLogs = if (currentLogs.size > 100) {
            currentLogs.takeLast(100) + entry
        } else {
            currentLogs + entry
        }
        _logs.value = nextLogs
    }

    fun getIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error getting IP", ex)
        }
        return "127.0.0.1"
    }
}
