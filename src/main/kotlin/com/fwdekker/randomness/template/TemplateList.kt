package com.fwdekker.randomness.template

import com.fwdekker.randomness.State
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
    var templates: List<Template> = DEFAULT_TEMPLATES
) : State() {
    override val name = "TemplateList"


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
        TemplateList(templates.map { it.deepCopy(retainUuid) })
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
                Template("Integer", listOf(IntegerScheme())),
                Template("Decimal", listOf(DecimalScheme())),
                Template("String", listOf(StringScheme())),
                Template("Word", listOf(WordScheme())),
                Template("UUID", listOf(UuidScheme()))
            )
    }
}
