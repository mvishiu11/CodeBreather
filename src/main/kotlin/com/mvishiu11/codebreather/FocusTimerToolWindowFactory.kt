package com.mvishiu11.codebreather

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import javax.swing.JTabbedPane
import java.awt.GridBagLayout
import java.awt.GridBagConstraints

/**
 * Main entry point for CodeBreather tool window.
 * This class organizes the Focus Timer, Session History, and Statistics panels in a tabbed interface.
 */
class FocusTimerToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val tabbedPane = JTabbedPane()

        // Add each panel as a tab
        tabbedPane.addTab("Focus Timer", FocusTimerPanel(project))
        tabbedPane.addTab("Session History", SessionHistoryPanel())
        tabbedPane.addTab("Session Stats", StatisticsPanel())

        val mainPanel = JBPanel<Nothing>().apply {
            layout = GridBagLayout()
            add(tabbedPane, GridBagConstraints().apply {
                fill = GridBagConstraints.BOTH
                weightx = 1.0
                weighty = 1.0
            })
        }

        toolWindow.contentManager.addContent(
            toolWindow.contentManager.factory.createContent(mainPanel, "", false)
        )
    }
}