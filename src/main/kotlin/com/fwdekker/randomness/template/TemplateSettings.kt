package com.fwdekker.randomness.template

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


    /**
     * Returns the template list.
     *
     * @return the template list
     */
    override fun getState() = templateList

    /**
     * Invokes [TemplateList.copyFrom].
     *
     * @param state the state to invoke [TemplateList.copyFrom] on
     */
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
