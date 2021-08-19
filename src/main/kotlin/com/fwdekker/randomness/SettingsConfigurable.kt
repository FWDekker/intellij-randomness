package com.fwdekker.randomness

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import javax.swing.JComponent


/**
 * Tells IntelliJ how to use a [StateEditor] in the settings dialog.
 */
@Suppress("LateinitUsage") // `createComponent` is invoked before any of the other methods
abstract class SettingsConfigurable : Configurable {
    /**
     * The user interface for changing the settings, displayed in IntelliJ's settings window.
     */
    lateinit var editor: StateEditor<*> private set


    /**
     * Returns true if the settings were modified since they were loaded or they are invalid.
     *
     * @return true if the settings were modified since they were loaded or they are invalid
     */
    override fun isModified() = editor.isModified() || editor.doValidate() != null

    /**
     * Saves the changes in the settings component to the default settings object.
     *
     * @throws ConfigurationException if the changes cannot be saved
     */
    @Throws(ConfigurationException::class)
    override fun apply() {
        val validationInfo = editor.doValidate()
        if (validationInfo != null)
            throw ConfigurationException(validationInfo, "Failed to save settings")

        editor.applyState()
    }

    /**
     * Discards unsaved changes in the settings component.
     */
    override fun reset() = editor.reset()


    /**
     * Creates a new editor and returns the root pane of the created editor.
     *
     * @return the root pane of the created editor
     */
    override fun createComponent(): JComponent =
        createEditor().let {
            editor = it
            it.rootComponent
        }

    /**
     * Creates a new editor.
     *
     * @return a new editor
     */
    abstract fun createEditor(): StateEditor<*>
}
