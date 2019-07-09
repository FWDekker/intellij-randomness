package com.fwdekker.randomness

import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JPanel


/**
 * Superclass for settings components.
 *
 * Subclasses **MUST** call `loadSettings` in their constructor.
 *
 * @param settings the settings to manage
 * @param <S> the type of settings managed by the subclass
 */
abstract class SettingsComponent<S : Settings<S>>(private val settings: S) : SettingsManager<S> {
    override fun loadSettings() = loadSettings(settings)

    override fun saveSettings() = saveSettings(settings)

    /**
     * Returns true if this component contains unsaved changes.
     *
     * @return true if this component contains unsaved changes
     */
    fun isModified() = settings.copyState().also { saveSettings(it) } != settings

    /**
     * Discards unsaved changes.
     */
    fun reset() = loadSettings()

    /**
     * Returns the panel containing the settings.
     *
     * @return the panel containing the settings
     */
    abstract fun getRootPane(): JPanel?

    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    abstract fun doValidate(): ValidationInfo?
}
