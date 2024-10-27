package com.mvishiu11.codebreather

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.File

object SessionLogger {
    private val file = File("session_history.json")
    private val json = Json { prettyPrint = true }

    fun logSession(stats: FocusSessionStats) {
        val sessions = loadSessions().toMutableList()
        sessions.add(stats)
        file.writeText(json.encodeToString(sessions))
    }

    fun loadSessions(): List<FocusSessionStats> {
        return if (file.exists()) {
            json.decodeFromString(file.readText())
        } else {
            emptyList()
        }
    }
}
