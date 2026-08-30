package com.example.models

import java.net.URLDecoder
import java.net.URLEncoder

data class User(
    val name: String,
    val lastSeen: Long = System.currentTimeMillis()
)

data class Message(
    val id: String,
    val sender: String,
    val receiver: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LogEntry(
    val id: String,
    val type: LogType,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class LogType {
    INFO, SUCCESS, WARNING, ERROR, MESSAGE
}

object JsonUtils {
    
    fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun serializeUser(user: User): String {
        return """{"name":"${escapeJson(user.name)}","lastSeen":${user.lastSeen}}"""
    }

    fun serializeUsers(users: List<User>): String {
        return users.joinToString(separator = ",", prefix = "[", postfix = "]") { serializeUser(it) }
    }

    fun serializeMessage(msg: Message): String {
        return """{"id":"${msg.id}","sender":"${escapeJson(msg.sender)}","receiver":"${escapeJson(msg.receiver)}","text":"${escapeJson(msg.text)}","timestamp":${msg.timestamp}}"""
    }

    fun serializeMessages(messages: List<Message>): String {
        return messages.joinToString(separator = ",", prefix = "[", postfix = "]") { serializeMessage(it) }
    }

    fun parseUsersJson(json: String): List<User> {
        val list = mutableListOf<User>()
        val nameRegex = """"name"\s*:\s*"([^"]*)"""".toRegex()
        val lastSeenRegex = """"lastSeen"\s*:\s*(\d+)""".toRegex()
        
        val objectMatches = """\{[^{}]*}""".toRegex().findAll(json)
        for (match in objectMatches) {
            val objStr = match.value
            val nameMatch = nameRegex.find(objStr)
            val lastSeenMatch = lastSeenRegex.find(objStr)
            if (nameMatch != null) {
                val name = nameMatch.groupValues[1]
                val lastSeen = lastSeenMatch?.groupValues[1]?.toLongOrNull() ?: System.currentTimeMillis()
                list.add(User(name, lastSeen))
            }
        }
        return list
    }

    fun parseMessagesJson(json: String): List<Message> {
        val list = mutableListOf<Message>()
        val idRegex = """"id"\s*:\s*"([^"]*)"""".toRegex()
        val senderRegex = """"sender"\s*:\s*"([^"]*)"""".toRegex()
        val receiverRegex = """"receiver"\s*:\s*"([^"]*)"""".toRegex()
        val textRegex = """"text"\s*:\s*"([^"]*)"""".toRegex()
        val timestampRegex = """"timestamp"\s*:\s*(\d+)""".toRegex()
        
        val objectMatches = """\{[^{}]*}""".toRegex().findAll(json)
        for (match in objectMatches) {
            val objStr = match.value
            val id = idRegex.find(objStr)?.groupValues?.get(1) ?: ""
            val sender = senderRegex.find(objStr)?.groupValues?.get(1) ?: ""
            val receiver = receiverRegex.find(objStr)?.groupValues?.get(1) ?: ""
            val text = textRegex.find(objStr)?.groupValues?.get(1) ?: ""
            val timestamp = timestampRegex.find(objStr)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            
            val unescapedText = text.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                
            list.add(Message(id, sender, receiver, unescapedText, timestamp))
        }
        return list
    }

    fun parseQueryParams(query: String?): Map<String, String> {
        if (query.isNullOrEmpty()) return emptyMap()
        val params = mutableMapOf<String, String>()
        val pairs = query.split("&")
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            if (idx > 0) {
                val key = pair.substring(0, idx)
                val value = pair.substring(idx + 1)
                try {
                    params[key] = URLDecoder.decode(value, "UTF-8")
                } catch (e: Exception) {
                    params[key] = value
                }
            }
        }
        return params
    }
}
