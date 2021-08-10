package com.fwdekker.randomness.template

import com.fwdekker.randomness.SchemeEditor
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import javax.swing.JComponent


/**
 * The user-configurable persistent collection of schemes applicable to generating arbitrary strings using template
 * syntax.
 *
 * @property templateList The list of templates to be stored persistently.
 * @see TemplateSettingsAction
 * @see TemplateSettingsConfigurable
 */
@State(
    name = "com.fwdekker.randomness.template.TemplateSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
class TemplateSettings : PersistentStateComponent<TemplateList> {
    private val templateList: TemplateList = TemplateList()


    override fun getState() = templateList

    override fun loadState(state: TemplateList) = templateList.copyFrom(state)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The persistent `TemplateSettings` instance.
         */
        val default: TemplateSettings
            get() = service()
    }
}


/**
 * The configurable for template settings, containing information on how the IDE should interact with the
 * [SchemeEditor].
 *
 * Set [templateToSelect] to determine which template should be selected when the configurable opens.
 *
 * @see TemplateListEditor
 * @see TemplateSettingsAction
 */
class TemplateSettingsConfigurable : Configurable {
    /**
     * The user interface for changing the settings, displayed in IntelliJ's settings window.
     */
    private lateinit var component: SchemeEditor<*>

    /**
     * The template to select after calling [createEditor].
     */
    var templateToSelect: String? = null


    /**
     * Returns the name of the configurable as displayed in the settings window.
     *
     * @return the name of the configurable as displayed in the settings window
     */
    override fun getDisplayName() = "Randomness"

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
    override fun createComponent(): JComponent =
        createEditor().let {
            component = it
            it.rootComponent
        }

    private fun createEditor() = TemplateListEditor().also { it.queueSelection = templateToSelect }
}
