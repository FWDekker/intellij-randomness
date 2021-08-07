package com.fwdekker.randomness.template

import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.MapAnnotation


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
    var templates: List<Template> = DEFAULT_TEMPLATES,
    @Suppress("unused") // At least two fields are required for serialization to work
    private val placeholder: String = ""
) : Settings<TemplateSettings>() {
    override fun getState() = this

    override fun deepCopy() =
        copy().also { copy ->
            copy.templates = templates.map { it.deepCopy() }
        }


    override fun doValidate(): String? {
        val duplicate =
            templates.firstOrNull { templates.indexOf(it) != templates.lastIndexOf(it) }?.name
        val invalid =
            templates.firstNotNullOfOrNull { template -> template.doValidate()?.let { "${template.name} > $it" } }

        return when {
            templates.isEmpty() -> "Configure at least one template."
            duplicate != null -> "There are multiple templates with the name '$duplicate'."
            invalid != null -> invalid
            else -> null
        }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: List<Template>
            get() = listOf(
                Template("The Integer", listOf(IntegerScheme())),
                Template("My String", listOf(LiteralScheme("start"), StringScheme(), LiteralScheme("end")))
            )

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
    override val component: TemplateSettingsEditor = TemplateSettingsEditor()
) : SettingsConfigurable<TemplateSettings>() {
    override fun getDisplayName() = "Randomness"
}
