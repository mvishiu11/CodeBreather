package com.mvishiu11.codebreather

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Serializable
data class FocusSessionStats(
    var linesAdded: Int = 0,
    var linesDeleted: Int = 0,
    var totalSessionTime: Long = 0L,
    val timestamp: Instant = Clock.System.now()
)