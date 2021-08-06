package com.fwdekker.randomness

import com.intellij.ide.DataManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.ex.Settings.KEY
import com.intellij.ui.components.ActionLink
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBEmptyBorder
import java.util.ResourceBundle
import javax.swing.Box
import javax.swing.JComponent


/**
 * A configurable to change settings of type [S].
 *
 * Allows the settings to be displayed in IntelliJ's settings window.
 *
 * @param S the type of settings the configurable changes.
 */
abstract class SettingsConfigurable<S : Settings<S>> : Configurable {
    /**
     * The user interface for changing the settings, displayed in IntelliJ's settings window.
     */
    protected abstract val component: StateEditor<S>


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

        component.applyState()
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


// TODO: Remove this configurable
/**
 * Randomness' root configurable; all other configurables are its children.
 */
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
     * Returns a panel containing links to the other configurables.
     *
     * @return a panel containing links to the other configurables
     */
    override fun createComponent() =
        panel {
            val children =
                Configurable.APPLICATION_CONFIGURABLE.extensionList
                    .single { it.id == "randomness.MainConfigurable" }
                    .children

            row {
                label(ResourceBundle.getBundle("randomness").getString("settings.main_text"))

                val manager = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(FIND_SETTINGS_TIMEOUT)
                val settings = manager?.let { KEY.getData(it) } ?: return@row
                row {
                    val box = Box.createVerticalBox().also { it() }
                    children
                        .mapNotNull { child -> settings.find(child.id)?.let { Pair(child.id, it) } }
                        .map { (id, child) ->
                            ActionLink(child.displayName ?: id) { settings.select(child) }
                                .apply { border = JBEmptyBorder(LINK_MARGIN_TOP, 0, LINK_MARGIN_BOTTOM, 0) }
                        }
                        .forEach { box.add(it) }
                }
            }
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * Maximum number of milliseconds to try to find current data manager instance.
         */
        const val FIND_SETTINGS_TIMEOUT = 1000

        /**
         * The margin to insert above each link in the panel.
         */
        const val LINK_MARGIN_TOP = 1

        /**
         * The margin to insert below each link in the panel.
         */
        const val LINK_MARGIN_BOTTOM = 3
    }
}
