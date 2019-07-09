package com.fwdekker.randomness

import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JComponent


/**
 * Superclass for settings dialogs.
 *
 * Subclasses **MUST** call `init` and `loadSettings` in their constructor.
 *
 * @param settings the settings to manage
 * @param <S> the type of settings managed by the subclass
 */
abstract class SettingsDialog<S : Settings<*>>(private val settings: S) : SettingsManager<S> {
    override fun loadSettings() = loadSettings(settings)

    override fun saveSettings() = saveSettings(settings)

    abstract fun getRootPane(): JComponent?

    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    abstract fun doValidate(): ValidationInfo?
}
