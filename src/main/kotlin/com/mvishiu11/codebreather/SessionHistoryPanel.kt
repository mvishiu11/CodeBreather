package com.mvishiu11.codebreather

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import kotlinx.datetime.*
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

/**
 * SessionHistoryPanel provides a ChatGPT-style expandable list of past sessions in a dark theme.
 */
class SessionHistoryPanel : JScrollPane() {

    init {
        val sessionHistoryPanel = JBPanel<Nothing>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = EmptyBorder(10, 10, 10, 10)
            background = Color(45, 45, 45)  // Dark background for main panel
        }

        val sessions = SessionLogger.loadSessions()
        sessions.forEach { session ->
            val sessionCard = createSessionCard(session)
            sessionHistoryPanel.add(sessionCard)
            sessionHistoryPanel.add(createVerticalSpacer(10))  // Add consistent space between cards
        }

        // Wrap sessionHistoryPanel in JScrollPane for scrolling
        this.viewport.add(sessionHistoryPanel)
        this.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        this.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        this.preferredSize = Dimension(450, 400)  // Adjusted size for improved readability
    }

    private fun createSessionCard(session: FocusSessionStats): JPanel {
        val sessionPanel = JPanel(BorderLayout()).apply {
            background = Color(60, 63, 65)  // Darker background for each session card
            border = LineBorder(Color(100, 100, 100), 1, true)  // Light gray border for card effect
            maximumSize = Dimension(400, 80)  // Fixed maximum size for consistency
            alignmentX = CENTER_ALIGNMENT
            isOpaque = true
        }

        // Header panel with date and session summary, acts as a toggle
        val headerPanel = JPanel(BorderLayout()).apply {
            background = Color(75, 75, 75)  // Dark gray background for header
            border = EmptyBorder(5, 10, 5, 10)
        }

        val sessionDate = session.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        headerPanel.add(JBLabel("Date: $sessionDate").apply {
            font = font.deriveFont(java.awt.Font.BOLD, 12f)
            foreground = Color(200, 200, 200)  // Light gray text for contrast
        }, BorderLayout.WEST)

        sessionPanel.add(headerPanel, BorderLayout.NORTH)

        // Detail panel with expanded session information, initially hidden
        val detailPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = EmptyBorder(10, 15, 10, 15)
            background = Color(60, 63, 65)  // Same dark color as the session card
            add(JBLabel("Time Spent: ${session.totalSessionTime / 1000}s").apply {
                foreground = Color(220, 220, 220)  // Light text for details
            })
            add(JBLabel("Lines Added: ${session.linesAdded}").apply {
                foreground = Color(220, 220, 220)
            })
            add(JBLabel("Lines Deleted: ${session.linesDeleted}").apply {
                foreground = Color(220, 220, 220)
            })
            isVisible = false  // Initially hidden; shown when header is clicked
        }
        sessionPanel.add(detailPanel, BorderLayout.CENTER)

        // Toggle detail visibility on header click
        headerPanel.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                detailPanel.isVisible = !detailPanel.isVisible
                sessionPanel.revalidate()
                sessionPanel.repaint()
            }
        })

        return sessionPanel
    }

    /**
     * Creates a spacer panel to add vertical space between session cards.
     */
    private fun createVerticalSpacer(height: Int): JPanel {
        return JPanel().apply {
            preferredSize = Dimension(1, height)
            background = Color(45, 45, 45)  // Matches background color of the session history panel
            isOpaque = true
        }
    }
}