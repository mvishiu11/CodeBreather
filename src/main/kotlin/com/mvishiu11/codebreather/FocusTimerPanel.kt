package com.mvishiu11.codebreather

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import kotlinx.coroutines.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JProgressBar
import java.util.concurrent.TimeUnit
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class FocusTimerPanel(private val project: Project) : JBPanel<Nothing>() {

    private var stats = FocusSessionStats()
    private var sessionStartTime: Long = 0L
    private var isSessionActive = false
    private var isPaused = false
    private lateinit var timerLabel: JBLabel
    private lateinit var progressBar: JProgressBar
    private lateinit var linesAddedLabel: JBLabel
    private lateinit var linesDeletedLabel: JBLabel
    private lateinit var timeSpentLabel: JBLabel
    private var job: Job? = null
    private var linesAdded = 0
    private var linesDeleted = 0

    init {
        layout = GridBagLayout()
        setupUI()
        initializeDocumentListener()
    }

    private fun setupUI() {
        val timeInput = JBTextField("25").apply { columns = 4 }
        val startButton = JButton("Start Focus Session")
        val pauseButton = JButton("Pause").apply { isEnabled = false }
        val endButton = JButton("End Session").apply { isEnabled = false }
        timerLabel = JBLabel("Time Remaining: 25:00").apply { font = font.deriveFont(18.0f) }
        progressBar = JProgressBar(0, 100).apply { isStringPainted = true }
        linesAddedLabel = JBLabel("Characters Added: 0")
        linesDeletedLabel = JBLabel("Lines Deleted: 0")
        timeSpentLabel = JBLabel("Time Spent: 0s")

        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }

        constraints.gridx = 0; constraints.gridy = 0
        add(JBLabel("Set Focus Time (minutes):"), constraints)
        constraints.gridx = 1
        add(timeInput, constraints)
        constraints.gridx = 0; constraints.gridy = 1; constraints.gridwidth = 2
        add(startButton, constraints)
        constraints.gridx = 0; constraints.gridy = 2; constraints.gridwidth = 2
        add(timerLabel, constraints)
        constraints.gridx = 0; constraints.gridy = 3; constraints.gridwidth = 2
        add(progressBar, constraints)
        constraints.gridy = 4
        add(linesAddedLabel, constraints)
        constraints.gridy = 5
        add(linesDeletedLabel, constraints)
        constraints.gridy = 6
        add(timeSpentLabel, constraints)
        constraints.gridy = 7; constraints.gridwidth = 1
        add(pauseButton, constraints)
        constraints.gridx = 1
        add(endButton, constraints)

        startButton.addActionListener {
            if (!isSessionActive) {
                val minutes = timeInput.text.toIntOrNull() ?: 25
                val totalMillis = TimeUnit.MINUTES.toMillis(minutes.toLong())
                sessionStartTime = System.currentTimeMillis()
                stats = FocusSessionStats()
                linesAdded = 0
                linesDeleted = 0
                isSessionActive = true
                isPaused = false
                startButton.isEnabled = false
                pauseButton.isEnabled = true
                endButton.isEnabled = true
                job = GlobalScope.launch(Dispatchers.Main) { startTimer(totalMillis) }
            }
        }

        pauseButton.addActionListener {
            isPaused = !isPaused
            pauseButton.text = if (isPaused) "Resume" else "Pause"
        }

        endButton.addActionListener {
            endSession()
            startButton.isEnabled = true
            pauseButton.isEnabled = false
            endButton.isEnabled = false
        }
    }

    private fun initializeDocumentListener() {
        val documentListener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                if (isSessionActive && !isPaused) {
                    val addedLines = event.newFragment.lines().count()
                    val deletedLines = event.oldFragment.lines().count()
                    linesAdded += addedLines
                    linesDeleted += deletedLines
                    linesAddedLabel.text = "Lines Added: $linesAdded"
                    linesDeletedLabel.text = "Lines Deleted: $linesDeleted"
                }
            }
        }
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener, project)
    }

    private suspend fun startTimer(totalMillis: Long) {
        var remainingMillis = totalMillis
        while (remainingMillis > 0 && isSessionActive) {
            delay(1000)
            if (!isPaused) {
                remainingMillis -= 1000
                val minutes = (remainingMillis / 60000).toInt()
                val seconds = (remainingMillis % 60000 / 1000).toInt()
                timerLabel.text = String.format("Time Remaining: %02d:%02d", minutes, seconds)
                val progress = (100 * (totalMillis - remainingMillis) / totalMillis).toInt()
                progressBar.value = progress
                val elapsedSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                timeSpentLabel.text = "Time Spent: ${elapsedSeconds}s"
            }
        }
        if (remainingMillis <= 0) endSession()
    }

    private fun endSession() {
        job?.cancel()
        isSessionActive = false
        stats.totalSessionTime = System.currentTimeMillis() - sessionStartTime
        stats.linesAdded = linesAdded
        stats.linesDeleted = linesDeleted
        SessionLogger.logSession(stats)
        NotificationHelper.showNotification(
            "Session Complete",
            "Total Time: ${stats.totalSessionTime / 1000}s, Lines Added: ${stats.linesAdded}, Lines Deleted: ${stats.linesDeleted}"
        )

        // Reset UI
        timerLabel.text = "Time Remaining: 00:00"
        progressBar.value = 0
        linesAddedLabel.text = "Characters Added: 0"
        linesDeletedLabel.text = "Characters Deleted: 0"
        timeSpentLabel.text = "Time Spent: 0s"
    }
}