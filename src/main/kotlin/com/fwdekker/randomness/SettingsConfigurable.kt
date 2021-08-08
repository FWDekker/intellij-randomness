package com.fwdekker.randomness

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import javax.swing.JComponent


/**
 * A configurable to change settings of some type.
 *
 * Allows the settings to be displayed in IntelliJ's settings window.
 */
abstract class SettingsConfigurable : Configurable {
    /**
     * The user interface for changing the settings, displayed in IntelliJ's settings window.
     */
    protected abstract val component: SchemeEditor<*>


    /**
     * Returns the name of the configurable as displayed in the settings window.
     *
     * @return the name of the configurable as displayed in the settings window
     */
    abstract override fun getDisplayName(): String

    /**
     * Returns true if the settings were modified since they were loaded or they are invalid.
     *
     * @return true if the settings were modified since they were loaded or they are invalid
     */
    override fun isModified() = component.isModified() || component.doValidate() != null

    /**
     * Saves the changes in the settings component to the default settings object.
     *
     * @throws ConfigurationException if the changes cannot be saved
     */
    @Throws(ConfigurationException::class)
    override fun apply() {
        val validationInfo = component.doValidate()
        if (validationInfo != null)
            throw ConfigurationException(validationInfo, "Failed to save settings")

        component.applyScheme()
    }

    /**
     * Discards unsaved changes in the settings component.
     */
    override fun reset() = component.reset()

    /**
     * Returns the root pane of the settings component.
     *
     * @return the root pane of the settings component
     */
    override fun createComponent(): JComponent? = component.rootComponent
}
