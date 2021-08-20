package com.fwdekker.randomness.template

import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.util.UUID


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
 * A configurable for editing [TemplateSettings].
 *
 * Set [templateToSelect] before [createComponent] is invoked to determine which template should be selected when the
 * configurable opens.
 *
 * @see TemplateListEditor
 * @see TemplateSettingsAction
 */
class TemplateSettingsConfigurable : SettingsConfigurable() {
    /**
     * The UUID of the template to select after calling [createEditor].
     */
    var templateToSelect: UUID? = null


    /**
     * Returns the name of the configurable as displayed in the settings window.
     *
     * @return the name of the configurable as displayed in the settings window
     */
    override fun getDisplayName() = "Randomness"

    override fun createEditor() = TemplateListEditor().also { it.queueSelection = templateToSelect }
}
