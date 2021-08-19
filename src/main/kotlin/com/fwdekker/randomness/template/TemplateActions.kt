package com.fwdekker.randomness.template

import com.fwdekker.randomness.InsertAction
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.options.ShowSettingsUtil
import icons.RandomnessIcons
import java.awt.event.ActionEvent


/**
 * All actions related to inserting template-based strings.
 *
 * @property template The template to create actions for.
 */
class TemplateGroupAction(
    private val template: Template = TemplateSettings.default.state.templates.first()
) : ActionGroup() {
    private val insertAction = TemplateInsertAction(template)
    private val repeatInsertAction = TemplateInsertAction(template, repeat = true)
    private val settingsAction = TemplateSettingsAction(template)


    /**
     * Sets the title of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.icon = template.icon ?: RandomnessIcons.Data.Base
        event.presentation.text = template.name
    }

    /**
     * Returns `true`.
     *
     * @param context carries information about the context of the invocation
     * @return `true`
     */
    override fun canBePerformed(context: DataContext) = true

    /**
     * Chooses one of the three actions to execute based on the key modifiers in [event].
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) =
        if (event.modifiers and ActionEvent.CTRL_MASK != 0) settingsAction.actionPerformed(event)
        else if (event.modifiers and ActionEvent.ALT_MASK != 0) repeatInsertAction.actionPerformed(event)
        else insertAction.actionPerformed(event)

    /**
     * Returns `true`.
     *
     * @return `true`
     */
    override fun isPopup() = true

    /**
     * Returns variant actions for the main insertion action.
     *
     * @param event carries information on the invocation place
     * @return variant actions for the main insertion action
     */
    override fun getChildren(event: AnActionEvent?) = arrayOf(repeatInsertAction, settingsAction)
}


/**
 * Inserts random strings in the editor based on the given template.
 *
 * @property template The template to use for inserting data.
 * @param repeat true if and only if the same value should be inserted at each caret
 * @see TemplateGroupAction
 */
class TemplateInsertAction(private val template: Template, private val repeat: Boolean = false) : InsertAction() {
    override val icon = template.icon

    override val name = (if (repeat) "Repeat " else "") + template.name


    override fun generateStrings(count: Int) = template.generateStrings(count)
}

/**
 * Open the settings dialog to edit the given template settings.
 *
 * @property template The template to select after opening the settings dialog.
 * @see TemplateGroupAction
 * @see TemplateSettingsConfigurable
 */
class TemplateSettingsAction(private val template: Template? = null) : AnAction() {
    /**
     * Sets the title of this action.
     *
     * @param event carries information on the invocation place
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.icon = template?.icons?.Settings ?: RandomnessIcons.Data.Settings
        event.presentation.text = "${if (template != null) template.name + " " else ""}Settings"
    }

    /**
     * Opens the IntelliJ settings menu at the right location to adjust the template configurable.
     *
     * @param event carries information on the invocation place
     */
    override fun actionPerformed(event: AnActionEvent) =
        ShowSettingsUtil.getInstance()
            .showSettingsDialog(event.project, TemplateSettingsConfigurable::class.java) { configurable ->
                configurable?.also { it.templateToSelect = template?.name }
            }
}
