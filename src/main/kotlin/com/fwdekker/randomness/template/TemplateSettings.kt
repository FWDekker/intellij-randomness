package com.fwdekker.randomness.template

import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service


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

    override fun loadState(state: TemplateList) = templateList.loadState(state)


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
 * The configurable for template settings.
 *
 * @see TemplateSettingsAction
 */
class TemplateSettingsConfigurable(
    override val component: TemplateListEditor = TemplateListEditor()
) : SettingsConfigurable() {
    override fun getDisplayName() = "Randomness"
}
