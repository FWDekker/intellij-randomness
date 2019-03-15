package com.fwdekker.randomness

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


/**
 * Shows a modal dialog for changing settings.
 */
abstract class SettingsAction : AnAction() {
    /**
     * The title of the dialog to display.
     */
    protected abstract val title: String


    override fun update(event: AnActionEvent?) {
        super.update(event)

        if (event != null)
            event.presentation.text = title
    }

    /**
     * Creates and displays a modal dialog for changing settings.
     *
     * @param event the event that triggered the creation of this dialog
     */
    override fun actionPerformed(event: AnActionEvent) {
        createDialog()
            .also {
                it.title = title
                it.show()
            }
    }


    /**
     * Returns the dialog to display.
     *
     * @return the dialog to display
     */
    protected abstract fun createDialog(): SettingsDialog<*>
}
