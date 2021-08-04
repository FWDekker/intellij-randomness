package com.fwdekker.randomness

import javax.swing.JPanel


abstract class SettingsComponent<S : Settings<S>>(private val settings: S) {
    /**
     * The panel containing the settings.
     */
    abstract val rootPane: JPanel?


    /**
     * Loads the default settings object.
     */
    fun loadSettings() = loadSettings(settings)

    /**
     * Loads `settings`.
     *
     * @param settings the settings to load
     */
    abstract fun loadSettings(settings: S)

    /**
     * Saves settings to the default settings object.
     */
    fun saveSettings() = saveSettings(settings)

    /**
     * Saves settings to `settings`.
     *
     * @param settings the settings to save to
     */
    abstract fun saveSettings(settings: S)


    /**
     * Returns true if this component contains unsaved changes.
     *
     * @return true if this component contains unsaved changes
     */
    fun isModified() = settings.deepCopy().also { saveSettings(it) } != settings || isModified(settings)

    /**
     * Returns true if this component contains unsaved changes.
     *
     * Implement this method only if meaningful changes can be made to the settings object that are not detected using
     * the settings' equals method.
     *
     * @param settings the settings as they were loaded into the component
     * @return true if this component contains unsaved changes
     */
    open fun isModified(settings: S): Boolean = false

    /**
     * Discards unsaved changes.
     */
    fun reset() = loadSettings()

    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    abstract fun doValidate(): ValidationInfo?


    /**
     * Adds a listener to these settings that is triggered when any of the settings' fields is changed.
     *
     * @param listener the function to invoke when any of the settings' fields is changed
     */
    abstract fun addChangeListener(listener: () -> Unit)
}

/**
 * A component to edit a [Scheme] with.
 *
 * @param S the type of scheme that can be edited
 */
abstract class SchemeComponent<S : Scheme<S>> {
    /**
     * The panel containing the settings components.
     */
    abstract val rootPane: JPanel?


    /**
     * Loads the settings from the given scheme into this component.
     *
     * @param scheme the scheme to load settings from
     */
    abstract fun loadScheme(scheme: S)

    /**
     * Returns the settings in this scheme as a scheme.
     *
     * @return the settings in this scheme as a scheme
     */
    abstract fun saveScheme(): S

    /**
     * Validates all input fields.
     *
     * @return `null` if the input is valid, or a `ValidationInfo` object explaining why the input is invalid
     */
    abstract fun doValidate(): ValidationInfo?


    /**
     * Adds a listener to these settings that is triggered when any of the settings' fields is changed.
     *
     * @param listener the function to invoke when any of the settings' fields is changed
     */
    abstract fun addChangeListener(listener: () -> Unit)
}
