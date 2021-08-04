package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * The user-configurable collection of schemes applicable to generating arbitrary strings using template syntax.
 *
 * @see TemplateSettingsAction
 * @see TemplateSettingsConfigurable
 */
@State(name = "TemplateSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class TemplateSettings(var templates: Template = DEFAULT_TEMPLATES) : Settings<TemplateSettings> {
    override fun deepCopy() = TemplateSettings(templates.deepCopy())

    override fun getState() = this

    override fun loadState(state: TemplateSettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Returns random template-based strings based on the descriptor.
     *
     * @param count the number of strings to generate
     * @return random template-based strings based on the descriptor
     */
    fun generateStrings(count: Int) = templates.generateStrings(count)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: Template
            get() = Template(
                listOf(
                    LiteralScheme("start"),
                    IntegerScheme(),
                    LiteralScheme("end")
                )
            )

        /**
         * The persistent `TemplateSettings` instance.
         */
        val default: TemplateSettings
            get() = service()
    }
}


data class Template(var schemes: List<Scheme<*>>) : Scheme<Template>() {
    override fun generateStrings(count: Int) =
        schemes.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { string -> data.joinToString(separator = "") { it[string] } } }

    override fun deepCopy() = Template(schemes.map { it.deepCopy() as Scheme<*> })
}


/**
 * The configurable for template settings.
 *
 * @see TemplateSettingsAction
 */
class TemplateSettingsConfigurable(
    override val component: TemplateSettingsComponent = TemplateSettingsComponent()
) : SettingsConfigurable<TemplateSettings>() {
    override fun getDisplayName() = "Templates"
}
