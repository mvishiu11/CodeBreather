package com.mvishiu11.codebreather

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

object NotificationHelper {
    private val messages = listOf(
        "Time to stretch those legs! You've earned it!",
        "Focus mode over! How about some coffee?",
        "Your code is on fire—now it's time for your brain to cool down."
    )

    fun showNotification(title: String, content: String) {
        Notifications.Bus.notify(
            Notification("FocusTimer", title, content, NotificationType.INFORMATION)
        )
    }

    fun showRandomBreakNotification() {
        val message = messages.random()
        showNotification("Break Time!", message)
    }
}