package com.fwdekker.randomness.template

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service


/**
 * The user-configurable persistent collection of schemes applicable to generating arbitrary strings using template
 * syntax.
 */
@State(
    name = "com.fwdekker.randomness.TemplateSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness-beta.xml")],
)
class TemplateListSettingsComponent : PersistentStateComponent<TemplateList> {
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
         * The persistent [TemplateListSettingsComponent] instance.
         */
        val default: TemplateListSettingsComponent
            get() = service()
    }
}
