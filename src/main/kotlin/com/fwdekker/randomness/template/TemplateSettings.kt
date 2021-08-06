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
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.XCollection


/**
 * The user-configurable persistent collection of schemes applicable to generating arbitrary strings using template
 * syntax.
 *
 * @property templates The templates defined by the user.
 * @property placeholder A placeholder value used to trick the serializer into working.
 * @see TemplateSettingsAction
 * @see TemplateSettingsConfigurable
 */
@State(
    name = "com.fwdekker.randomness.template.TemplateSettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
data class TemplateSettings(
    @MapAnnotation(sortBeforeSave = false)
    var templates: Map<String, Template> = DEFAULT_TEMPLATES,
    @Suppress("unused") // At least two fields are required for serialization to work
    private val placeholder: String = ""
) : Settings<TemplateSettings>() {
    override fun getState() = this

    override fun deepCopy() =
        copy().also { copy ->
            copy.templates = templates.map { it.key to it.value.deepCopy() }.toMap()
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: Map<String, Template>
            get() = mapOf(
                "The Integer" to Template(listOf(IntegerScheme())),
                "My String" to Template(listOf(LiteralScheme("start"), StringScheme(), LiteralScheme("end")))
            )

        /**
         * The persistent `TemplateSettings` instance.
         */
        val default: TemplateSettings
            get() = service()
    }
}

/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property schemes The ordered list of underlying schemes.
 */
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
        schemes.onEach { it.random = random }.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { i -> data.joinToString(separator = "") { it[i] } } }


    override fun deepCopy() =
        copy().also { copy -> copy.schemes = this.schemes.map { it.deepCopy() } }


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
    override val component: TemplateSettingsEditor = TemplateSettingsEditor()
) : SettingsConfigurable<TemplateSettings>() {
    override fun getDisplayName() = "Templates"
}
