package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.MapAnnotation


/**
 * A collection of different templates.
 *
 * @property templates the collection of templates, each with a unique [Template.name]
 * @see TemplateSettings
 */
data class TemplateList(
    @MapAnnotation(sortBeforeSave = false)
    var templates: MutableList<Template> = DEFAULT_TEMPLATES.toMutableList()
) : Settings() {
    /**
     * Sets the [SettingsState] for each template in this list and returns this instance.
     *
     * @param settingsState the settings state for each template in this list
     * @return this instance
     */
    fun applySettingsState(settingsState: SettingsState): TemplateList {
        templates.forEach { it.setSettingsState(settingsState) }
        return this
    }


    /**
     * Find a recursive path of templates including each other, starting at [reference].
     *
     * @param reference the reference to start searching at
     * @return a recursive path of templates including each other starting at [reference], or `null` if there is no such
     * path
     */
    fun findRecursionFrom(reference: TemplateReference) = findRecursionFrom(reference, mutableListOf())

    /**
     * @see findRecursionFrom
     */
    private fun findRecursionFrom(
        reference: TemplateReference,
        history: MutableList<TemplateReference>
    ): List<Template>? {
        if (reference in history) return listOf(reference.parent)
        history += reference

        return reference.template?.schemes
            ?.filterIsInstance<TemplateReference>()
            ?.firstNotNullOfOrNull { findRecursionFrom(it, history) }
            ?.let { listOf(reference.parent) + it }
    }


    override fun doValidate(): String? {
        val duplicate =
            templates.firstOrNull { templates.indexOf(it) != templates.lastIndexOf(it) }?.name
        val invalid =
            templates.firstNotNullOfOrNull { template -> template.doValidate()?.let { "${template.name} > $it" } }

        return when {
            duplicate != null -> "There are multiple templates with the name '$duplicate'."
            invalid != null -> invalid
            else -> null
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        TemplateList(templates.map { it.deepCopy(retainUuid) }.toMutableList())
            .also { if (retainUuid) it.uuid = this.uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_TEMPLATES: List<Template>
            get() = listOf(
                Template("Integer", mutableListOf(IntegerScheme())),
                Template("Decimal", mutableListOf(DecimalScheme())),
                Template("String", mutableListOf(StringScheme())),
                Template("Word", mutableListOf(WordScheme())),
                Template("UUID", mutableListOf(UuidScheme()))
            )


        /**
         * Constructs a [TemplateList] consisting of a single template with the given schemes.
         *
         * @param schemes the schemes to give to the list's single template
         * @param name the name of the template
         */
        fun from(vararg schemes: Scheme, name: String = Template.DEFAULT_NAME) =
            TemplateList(mutableListOf(Template(name, schemes.toMutableList())))
    }
}
