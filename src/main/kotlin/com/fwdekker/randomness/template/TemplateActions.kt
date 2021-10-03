package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.InsertAction
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Timely.generateTimely
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.array.ArrayDecoratorEditor
import com.fwdekker.randomness.ui.PreviewPanel
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ShowSettingsUtil
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JPanel


/**
 * All actions related to inserting template-based strings.
 *
 * @property template The template to create actions for.
 * @see TemplateInsertAction
 * @see TemplateSettingsAction
 */
class TemplateGroupAction(private val template: Template) : ActionGroup() {
    /**
     * Returns the action that is appropriate for the given keyboard modifiers.
     *
     * @param array `true` if and only if the output should be in array form
     * @param repeat `true` if and only if the output should be repeated
     * @param settings `true` if and only if settings should be shown
     */
    private fun getActionByModifier(array: Boolean = false, repeat: Boolean = false, settings: Boolean = false) =
        if (settings) TemplateSettingsAction(template)
        else TemplateInsertAction(template, array = array, repeat = repeat)


    /**
     * Sets the title of this action.
     *
     * @param event carries contextual information
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.icon = template.icon
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
     * @param event carries contextual information
     */
    override fun actionPerformed(event: AnActionEvent) =
        getActionByModifier(
            array = event.modifiers and event.modifiers and ActionEvent.SHIFT_MASK != 0,
            repeat = event.modifiers and event.modifiers and ActionEvent.ALT_MASK != 0,
            settings = event.modifiers and event.modifiers and ActionEvent.CTRL_MASK != 0
        ).actionPerformed(event)

    /**
     * Returns `true`.
     *
     * @return `true`
     */
    override fun isPopup() = true

    /**
     * Returns variant actions for the main insertion action.
     *
     * @param event carries contextual information
     * @return variant actions for the main insertion action
     */
    override fun getChildren(event: AnActionEvent?) =
        arrayOf(
            getActionByModifier(array = true),
            getActionByModifier(repeat = true),
            getActionByModifier(array = true, repeat = true),
            getActionByModifier(settings = true)
        )
}


/**
 * Inserts random strings in the editor using [template].
 *
 * @property template The template to use for inserting data.
 * @property array `true` if and only if an array of values should be inserted.
 * @property repeat `true` if and only if the same value should be inserted at each caret.
 * @see TemplateGroupAction
 */
open class TemplateInsertAction(
    private val template: Template,
    private val array: Boolean = false,
    private val repeat: Boolean = false
) : InsertAction() {
    override val icon
        get() = template.icon?.let { if (repeat) it.plusOverlay(OverlayIcon.REPEAT) else it }

    override val name
        get() =
            template.name
                .let { if (array) Bundle("template.name.array_suffix", it) else it }
                .let { if (repeat) Bundle("template.name.repeat_prefix", it) else it }

    override val configurable
        get() =
            if (array) ArrayDecoratorConfigurable(template.arrayDecorator)
            else null


    override fun generateStrings(count: Int) =
        generateTimely {
            val template = template.deepCopy().also { it.arrayDecorator.enabled = array }

            if (repeat) template.generateStrings().single().let { string -> List(count) { string } }
            else template.generateStrings(count)
        }


    /**
     * Edits the [ArrayDecorator] of a template and shows a preview.
     *
     * @param arrayDecorator the decorator being edited in this configurable
     */
    inner class ArrayDecoratorConfigurable(arrayDecorator: ArrayDecorator) : Configurable {
        private val editor = ArrayDecoratorEditor(arrayDecorator, false)
        private val previewPanel = PreviewPanel {
            template.deepCopy().also {
                it.arrayDecorator = editor.readState()
                it.arrayDecorator.enabled = true
            }
        }


        init {
            editor.addChangeListener { previewPanel.updatePreview() }
            previewPanel.updatePreview()
        }


        override fun getPreferredFocusedComponent() = editor.preferredFocusedComponent

        override fun createComponent() =
            JPanel(BorderLayout())
                .also {
                    it.add(editor.rootComponent, BorderLayout.NORTH)
                    it.add(previewPanel.rootComponent, BorderLayout.SOUTH)
                }

        override fun isModified() = editor.isModified()

        override fun apply() = editor.applyState()

        override fun getDisplayName() = name

        override fun disposeUIResources() {
            editor.dispose()
            previewPanel.dispose()
        }
    }
}

/**
 * Open the settings dialog to edit [template].
 *
 * @property template The template to select after opening the settings dialog.
 * @see TemplateGroupAction
 * @see TemplateSettingsConfigurable
 */
class TemplateSettingsAction(private val template: Template? = null) : AnAction() {
    /**
     * Sets the title of this action.
     *
     * @param event carries contextual information
     */
    override fun update(event: AnActionEvent) {
        super.update(event)

        event.presentation.icon =
            if (template?.icon == null) RandomnessIcons.SETTINGS
            else template.icon!!.plusOverlay(OverlayIcon.SETTINGS)
        event.presentation.text =
            if (template == null) Bundle("template.name.settings")
            else Bundle("template.name.settings_suffix", template.name)
    }

    /**
     * Opens the IntelliJ settings menu at the right location to adjust the template configurable.
     *
     * @param event carries contextual information
     */
    override fun actionPerformed(event: AnActionEvent) =
        ShowSettingsUtil.getInstance()
            .showSettingsDialog(event.project, TemplateSettingsConfigurable::class.java) { configurable ->
                configurable?.also { it.templateToSelect = template?.uuid }
            }
}
