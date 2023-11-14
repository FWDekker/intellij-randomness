package com.fwdekker.randomness

import com.fwdekker.randomness.template.TemplateListConfigurable
import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


/**
 * Displays notifications when a project is opened.
 */
class Notifier : StartupActivity {
    /**
     * Shows startup notifications.
     */
    override fun runActivity(project: Project) = showWelcomeToV3(project)

    /**
     * Shows a notification introducing the user to version 3 of Randomness.
     */
    private fun showWelcomeToV3(project: Project) {
        val key = "notifications.welcome_to_v3"

        val propComp = PropertiesComponent.getInstance()
        val propKey = "com.fwdekker.randomness.$key"
        if (propComp.isTrueValue(propKey))
            return
        propComp.setValue(propKey, true)

        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.fwdekker.randomness.updates")
            .createNotification(Bundle("$key.title"), Bundle("$key.content"), NotificationType.INFORMATION)
            .setSubtitle(Bundle("$key.subtitle"))
            .setIcon(Icons.RANDOMNESS)
            .addAction(object : NotificationAction(Bundle("$key.usage")) {
                override fun actionPerformed(event: AnActionEvent, notification: Notification) =
                    BrowserUtil.browse(Bundle("$key.usage_url"))
            })
            .addAction(object : NotificationAction(Bundle("$key.changes")) {
                override fun actionPerformed(event: AnActionEvent, notification: Notification) =
                    BrowserUtil.browse(Bundle("$key.changes_url"))
            })
            .addAction(object : NotificationAction(Bundle("$key.settings")) {
                override fun actionPerformed(event: AnActionEvent, notification: Notification) =
                    ShowSettingsUtil.getInstance()
                        .showSettingsDialog(event.project, TemplateListConfigurable::class.java)
            })
            .notify(project)
    }
}
