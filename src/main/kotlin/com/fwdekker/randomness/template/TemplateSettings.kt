package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection


/**
 * The user-configurable collection of schemes applicable to generating arbitrary strings using template syntax.
 *
 * @see TemplateSettingsAction
 * @see TemplateSettingsConfigurable
 */
@State(
    name = "com.fwdekker.randomness.template.TemplateSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
class TemplateSettings(
    var templates: List<Template> = DEFAULT_TEMPLATES.map { it.deepCopy() },
    @Suppress("unused") // At least two fields are required for serialization to work
    val placeholder: String = ""
) : Settings<TemplateSettings> {
    override fun deepCopy() = TemplateSettings(templates.map { it.deepCopy() })

    override fun getState() = this

    override fun loadState(state: TemplateSettings) = XmlSerializerUtil.copyBean(state, this)


    fun generateStrings(count: Int) = templates.first().generateStrings(count)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: List<Template>
            get() = listOf(
                Template(listOf(IntegerScheme())),
                Template(listOf(LiteralScheme("start"), StringScheme(), LiteralScheme("end")))
            )

        /**
         * The persistent `TemplateSettings` instance.
         */
        val default: TemplateSettings
            get() = service()
    }
}


data class Template(
    @get:XCollection(
        elementTypes = [
            IntegerScheme::class,
            DecimalScheme::class,
            StringScheme::class,
            WordScheme::class,
            UuidScheme::class,
            LiteralScheme::class
        ]
    )
    var schemes: List<Scheme<*>> = DEFAULT_SCHEMES
) : Scheme<Template>() {
    override fun generateStrings(count: Int) =
        schemes.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { string -> data.joinToString(separator = "") { it[string] } } }

    override fun deepCopy() = Template(schemes.map { it.deepCopy() as Scheme<*> })


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_SCHEMES: List<Scheme<*>>
            get() = listOf(IntegerScheme())
    }
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
