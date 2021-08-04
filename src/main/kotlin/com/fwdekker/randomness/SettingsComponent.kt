package com.fwdekker.randomness

import javax.swing.JPanel


/**
 * A component that allows the user to edit settings and its corresponding schemes.
 *
 * Subclasses **MUST** call `loadSettings` in their constructor.
 *
 * There are multiple settings [S] instances at any time. The `settings` given in the constructor is read when the
 * component is created and is written to when the user saves its changes. The currently-selected scheme is loaded into
 * the component's inputs. When the user selects a different scheme of which to change its values, the values in the
 * input fields are stored in a copy of `settings`. This way, the local changes are not lost when switching between
 * schemes, and the user can still revert all unsaved changes if desired.
 *
 * @param S the type of settings to manage
 * @param T the type of scheme to manage
 * @param settings the settings to manage
 */
abstract class SettingsComponent<S : Settings<S>>(private val settings: S) : SettingsManager<S> {
    /**
     * The panel containing the settings.
     */
    abstract val rootPane: JPanel?


    final override fun loadSettings() = loadSettings(settings)

    final override fun saveSettings() = saveSettings(settings)


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
