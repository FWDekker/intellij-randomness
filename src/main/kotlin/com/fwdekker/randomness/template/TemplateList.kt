package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.MapAnnotation


/**
 * A collection of different templates.
 *
 * Not actually a [Scheme] because its [generateStrings] method will always return empty strings. However, it can be
 * used in a [com.fwdekker.randomness.SchemeEditor] like other schemes.
 *
 * @property templates the collection of templates, each with a unique [Template.name]
 * @see TemplateSettings
 */
data class TemplateList(
    @MapAnnotation(sortBeforeSave = false)
    var templates: List<Template> = DEFAULT_TEMPLATES
) : Scheme() {
    override val decorator: Nothing? = null
    override val name = "TemplateList"


    /**
     * Returns a list of [count] empty strings.
     *
     * To generate strings using a template, select it from the [templates] field.
     *
     * @param count the number of empty strings to return
     * @return a list of [count] empty strings
     */
    override fun generateUndecoratedStrings(count: Int) = List(count) { "" }


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

    override fun deepCopy() = TemplateList(templates.map { it.deepCopy() })


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
