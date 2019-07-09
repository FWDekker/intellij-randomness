package com.fwdekker.randomness

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel


/**
 * Randomness' root configurable; all other configurables are its children.
 */
// TODO Populate this.
class RandomnessConfigurable : Configurable {
    /**
     * Returns the name of the configurable as displayed in the settings window.
     *
     * @return the name of the configurable as displayed in the settings window
     */
    override fun getDisplayName() = "Randomness"

    /**
     * Returns false because there is nothing to be modified.
     *
     * @return false because there is nothing to be modified
     */
    override fun isModified() = false

    /**
     * Does nothing because nothing can be done.
     */
    override fun apply() = Unit

    /**
     * Returns an empty panel.
     *
     * @return an empty panel
     */
    override fun createComponent() = JPanel()
}


/**
 * A configurable to change settings of type [S].
 *
 * @param S the type of settings the configurable changes.
 */
abstract class SettingsConfigurable<S : Settings<S>> : Configurable {
    /**
     * The user interface for changing the settings.
     */
    protected abstract val dialog: SettingsDialog<S>


    /**
     * Returns the name of the configurable as displayed in the settings window.
     *
     * @return the name of the configurable as displayed in the settings window
     */
    abstract override fun getDisplayName(): String

    /**
     * Returns true if the settings were modified since they were loaded.
     *
     * @return true if the settings were modified since they were loaded
     */
    override fun isModified() = dialog.isModified()

    /**
     * Saves the user's changes to the default settings object.
     */
    override fun apply() = dialog.saveSettings()

    /**
     * Discards unsaved changes.
     */
    override fun reset() = dialog.reset()

    /**
     * Returns the root pane of the settings interface.
     *
     * @return the root pane of the settings interface
     */
    override fun createComponent(): JComponent? = dialog.getRootPane()
}
