package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.Disposer
import javax.swing.JComponent


/**
 * Tells IntelliJ how to use a [TemplateListEditor] to edit a [TemplateList] in the settings dialog.
 *
 * Set [templateToSelect] before [createComponent] is invoked to determine which template should be selected when the
 * configurable opens.
 *
 * This class is separate from [TemplateListEditor] because that class creates UI components in the constructor. But
 * configurables may be created at any time in the background, so using [TemplateListEditor] as a configurable would
 * cause unnecessary lag.
 *
 * @see TemplateSettingsAction
 */
class TemplateListConfigurable : Configurable, Disposable {
    /**
     * The user interface for changing the settings, displayed in IntelliJ's settings window.
     */
    @Suppress("detekt:LateinitUsage") // Initialized in `createComponent`
    lateinit var editor: TemplateListEditor private set

    /**
     * The UUID of the template to select after calling [createComponent].
     */
    var templateToSelect: String? = null


    /**
     * Returns `true` if the settings were modified since they were loaded or they are invalid.
     */
    override fun isModified() = editor.isModified() || editor.doValidate() != null

    /**
     * Saves the changes in the settings component to the default settings object, and updates template shortcuts.
     *
     * @throws ConfigurationException if the changes cannot be saved
     */
    @Throws(ConfigurationException::class)
    override fun apply() {
        val validationInfo = editor.doValidate()
        if (validationInfo != null)
            throw ConfigurationException(validationInfo, Bundle("template_list.error.failed_to_save_settings"))

        val oldList = editor.originalTemplateList.deepCopy(retainUuid = true)
        editor.apply()
        val newList = editor.originalTemplateList.deepCopy(retainUuid = true)

        TemplateActionLoader().updateActions(oldList.templates, newList.templates)
    }

    /**
     * Discards unsaved changes in the settings component.
     */
    override fun reset() = editor.reset()

    /**
     * Recursively disposes this configurable's resources.
     */
    override fun disposeUIResources() = Disposer.dispose(this)

    /**
     * Non-recursively disposes this configurable's resources.
     */
    override fun dispose() = Unit


    /**
     * Creates a new editor and returns the root pane of the created editor.
     */
    override fun createComponent(): JComponent {
        editor = TemplateListEditor().also { Disposer.register(this, it) }
        editor.queueSelection = templateToSelect
        return editor.rootComponent
    }


    /**
     * Returns the name of the configurable as displayed in the settings window.
     */
    override fun getDisplayName() = "Randomness"
}
