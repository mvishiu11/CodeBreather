package com.mvishiu11.codebreather

import kotlinx.datetime.*
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryToolTipGenerator
import org.jfree.chart.title.TextTitle
import org.jfree.data.category.DefaultCategoryDataset
import javax.swing.JComboBox
import javax.swing.JPanel
import com.intellij.ui.components.JBLabel
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets

/**
 * StatisticsPanel provides separate graphs for time spent and lines added/deleted,
 * with filters for grouping by day, hour, or minute, and displays text-based statistics.
 */
class StatisticsPanel : JPanel() {

    private val timeDataset = DefaultCategoryDataset()
    private val lineDataset = DefaultCategoryDataset()
    private val totalTimeLabel = JBLabel()
    private val totalLinesLabel = JBLabel()

    // Declare the chart panels as properties for access in updateDataset()
    private val timeChartPanel: ChartPanel
    private val lineChartPanel: ChartPanel

    init {
        layout = GridBagLayout()

        val filterOptions = listOf("Day", "Hour", "Minute", "All Time")
        val filterDropdown = JComboBox(filterOptions.toTypedArray())
        val filterLabel = JBLabel("Group Stats By:")

        // Initialize the charts and chart panels
        val timeChart = createStyledChart("Time Spent per Session", "Time (s)", timeDataset)
        val lineChart = createStyledChart("Lines Added/Deleted per Session", "Lines", lineDataset)

        timeChartPanel = ChartPanel(timeChart).apply { preferredSize = Dimension(500, 350) }
        lineChartPanel = ChartPanel(lineChart).apply { preferredSize = Dimension(500, 350) }

        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }

        constraints.gridx = 0; constraints.gridy = 0
        add(filterLabel, constraints)
        constraints.gridx = 1
        add(filterDropdown, constraints)
        constraints.gridx = 0; constraints.gridy = 1; constraints.gridwidth = 2
        add(timeChartPanel, constraints)
        constraints.gridy = 2
        add(lineChartPanel, constraints)

        // Add basic stats labels below the graphs
        constraints.gridy = 3
        add(totalTimeLabel, constraints)
        constraints.gridy = 4
        add(totalLinesLabel, constraints)

        // Set initial stats and chart data
        filterDropdown.addActionListener {
            val selectedOption = filterDropdown.selectedItem as String
            updateDataset(selectedOption)
            displayBasicStats(selectedOption)
        }

        // Initialize with "All Time" data
        updateDataset("All Time")
        displayBasicStats("All Time")
    }

    /**
     * Creates a styled chart with a custom look and feel for better readability.
     */
    private fun createStyledChart(title: String, yAxisLabel: String, dataset: DefaultCategoryDataset): org.jfree.chart.JFreeChart {
        val chart = ChartFactory.createBarChart(
            title,
            "Session",
            yAxisLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        )

        // Customize title
        chart.title.font = Font("SansSerif", Font.BOLD, 14)

        // Customize plot
        val plot = chart.categoryPlot.apply {
            domainAxis.categoryLabelPositions = CategoryLabelPositions.UP_45  // Rotate labels
            domainAxis.labelFont = Font("SansSerif", Font.PLAIN, 12)
            rangeAxis.labelFont = Font("SansSerif", Font.PLAIN, 12)
            rangeAxis.standardTickUnits = NumberAxis.createIntegerTickUnits()
            backgroundPaint = Color(235, 235, 235)  // Light background
            domainGridlinePaint = Color.GRAY
            rangeGridlinePaint = Color.GRAY
        }

        // Customize renderer for bars
        val renderer = BarRenderer().apply {
            setSeriesPaint(0, Color(79, 129, 189))  // Blue for first dataset
            setSeriesPaint(1, Color(192, 80, 77))   // Red for second dataset
            maximumBarWidth = 0.1  // Control bar width
            defaultToolTipGenerator = StandardCategoryToolTipGenerator()  // Tooltips on hover
        }
        plot.renderer = renderer

        return chart
    }

    /**
     * Updates the dataset for both time and line charts based on the selected grouping.
     * Aggregates sessions by day, hour, or minute.
     */
    private fun updateDataset(groupBy: String) {
        val sessions = SessionLogger.loadSessions()
        val timeZone = TimeZone.currentSystemDefault()

        // Clear previous data
        timeDataset.clear()
        lineDataset.clear()

        val groupedSessions = sessions.groupBy { session ->
            when (groupBy) {
                "Day" -> session.timestamp.toLocalDateTime(timeZone).date
                "Hour" -> session.timestamp.toLocalDateTime(timeZone).let { it.date to it.hour }
                "Minute" -> session.timestamp.toLocalDateTime(timeZone).let { it.date to it.hour to it.minute }
                else -> null
            }
        }

        groupedSessions.forEach { (key, sessionsInGroup) ->
            val label = when (key) {
                is LocalDate -> "$key (${sessionsInGroup.size} sessions)"
                is Pair<*, *> -> "${key.first} ${key.second}h (${sessionsInGroup.size} sessions)"
                is Triple<*, *, *> -> "${key.first} ${key.second}h${key.third}m"
                else -> "All Time"
            }

            val totalSessionTime = sessionsInGroup.sumOf { it.totalSessionTime / 1000 } // in seconds
            val totalLinesAdded = sessionsInGroup.sumOf { it.linesAdded }
            val totalLinesDeleted = sessionsInGroup.sumOf { it.linesDeleted }

            timeDataset.addValue(totalSessionTime, "Time Spent (s)", label)
            lineDataset.addValue(totalLinesAdded, "Lines Added", label)
            lineDataset.addValue(totalLinesDeleted, "Lines Deleted", label)
        }
    }

    /**
     * Displays basic text-based statistics such as total time spent and total lines added/deleted.
     * Aggregates based on the selected grouping.
     */
    private fun displayBasicStats(groupBy: String) {
        val sessions = SessionLogger.loadSessions()
        val timeZone = TimeZone.currentSystemDefault()

        // Group sessions by the selected time unit for text stats as well
        val groupedSessions = sessions.groupBy { session ->
            when (groupBy) {
                "Day" -> session.timestamp.toLocalDateTime(timeZone).date
                "Hour" -> session.timestamp.toLocalDateTime(timeZone).let { it.date to it.hour }
                "Minute" -> session.timestamp.toLocalDateTime(timeZone).let { it.date to it.hour to it.minute }
                else -> null
            }
        }

        // Calculate total statistics across all sessions
        val totalSessions = sessions.size
        val totalTimeSpent = sessions.sumOf { it.totalSessionTime / 1000 } // in seconds
        val totalLinesAdded = sessions.sumOf { it.linesAdded }
        val totalLinesDeleted = sessions.sumOf { it.linesDeleted }

        // Update labels
        totalTimeLabel.text = "Total Time Spent: $totalTimeSpent seconds across $totalSessions sessions"
        totalLinesLabel.text = "Total Lines Added: $totalLinesAdded, Total Lines Deleted: $totalLinesDeleted"
    }
}