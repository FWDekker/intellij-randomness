package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.State
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.DefaultWordList
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.MapAnnotation


/**
 * A collection of different templates.
 *
 * @property templates The collection of templates, each with a unique name.
 */
data class TemplateList(
    @MapAnnotation(sortBeforeSave = false)
    var templates: List<Template> = DEFAULT_TEMPLATES.toMutableList(),
) : State() {
    override fun applyContext(context: Box<Settings>) {
        super.applyContext(context)
        templates.forEach { it.applyContext(context) }
    }


    // TODO: Document this
    // Returns all templates that [reference] could reference if its target were changed.
    fun listValidReferenceTargets(reference: TemplateReference): List<Template> {
        val listCopy = this.deepCopy(retainUuid = true)
        listCopy.applyContext(Settings(listCopy))

        val referenceCopy = listCopy.getSchemeByUuid(reference.uuid) as? TemplateReference
            ?: error("TODO")

        return listCopy.templates
            .filter {
                referenceCopy.template = it
                listCopy.findRecursionFrom(referenceCopy) == null
            }
            .map { listCopy.getTemplateByUuid(it.uuid)!! }
    }

    // TODO: Document this
    // Returns all templates that [template] could reference given a new reference added to [template].
    fun listValidReferenceTargets(template: Template): List<Template> {
        val listCopy = this.deepCopy(retainUuid = true)
        listCopy.applyContext(Settings(listCopy))

        val templateCopy = listCopy.getTemplateByUuid(template.uuid) ?: error("TODO")
        val testReference = TemplateReference().also { it.applyContext(templateCopy.context) }
        templateCopy.schemes += testReference

        return listCopy.templates
            .filter {
                testReference.template = it
                listCopy.findRecursionFrom(testReference) == null
            }
            .map { listCopy.getTemplateByUuid(it.uuid)!! }
    }

    /**
     * Find a recursive path of templates that include each other, starting at [reference].
     *
     * @param reference the reference to start searching at
     * @return a recursive path of templates that include each other starting at [reference], or `null` if there is no
     * such path
     */
    fun findRecursionFrom(reference: TemplateReference) = findRecursionFrom(reference, mutableListOf())

    /**
     * @see findRecursionFrom
     */
    private fun findRecursionFrom(
        reference: TemplateReference,
        history: MutableList<TemplateReference>,
    ): List<Template>? {
        if (reference in history) return listOf(reference.parent)
        history += reference

        return reference.template?.schemes
            ?.filterIsInstance<TemplateReference>()
            ?.firstNotNullOfOrNull { findRecursionFrom(it, history) }
            ?.let { listOf(reference.parent) + it }
    }


    /**
     * Returns the template in this list with [uuid] as its UUID.
     *
     * @param uuid the UUID to search for
     * @return the template in this list with [uuid] as its UUID
     */
    fun getTemplateByUuid(uuid: String) = templates.singleOrNull { it.uuid == uuid }

    /**
     * Returns the template or scheme in this list with [uuid] as its UUID.
     *
     * @param uuid the UUID to search for
     * @return the template or scheme in this list with [uuid] as its UUID
     */
    fun getSchemeByUuid(uuid: String) = templates.flatMap { listOf(it) + it.schemes }.singleOrNull { it.uuid == uuid }


    override fun doValidate(): String? {
        val templateNames = templates.map { it.name }
        val duplicate = templateNames.firstOrNull { templateNames.indexOf(it) != templateNames.lastIndexOf(it) }
        val invalid =
            templates.firstNotNullOfOrNull { template -> template.doValidate()?.let { "${template.name} > $it" } }

        return when {
            duplicate != null -> Bundle("template_list.error.duplicate_name", duplicate)
            invalid != null -> invalid
            else -> null
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(templates = templates.map { it.deepCopy(retainUuid) }).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates] field.
         */
        val DEFAULT_TEMPLATES: List<Template>
            get() = listOf(
                Template("Integer", listOf(IntegerScheme())),
                Template("Decimal", listOf(DecimalScheme())),
                Template("String", listOf(StringScheme())),
                Template("UUID", listOf(UuidScheme())),
                Template("Date-Time", listOf(DateTimeScheme())),
                Template("Hex color", listOf(StringScheme(pattern = "#[0-9a-f]{6}"))),
                Template(
                    "Name",
                    listOf(
                        WordScheme(
                            words = DefaultWordList.WORD_LIST_MAP["Forenames"]!!.words,
                            affixDecorator = AffixDecorator(enabled = true, descriptor = "@ "),
                        ),
                        WordScheme(words = DefaultWordList.WORD_LIST_MAP["Surnames"]!!.words),
                    )
                ),
                Template(
                    "Lorem Ipsum",
                    listOf(
                        WordScheme(
                            words = DefaultWordList.WORD_LIST_MAP["Lorem"]!!.words,
                            capitalization = CapitalizationMode.FIRST_LETTER,
                            affixDecorator = AffixDecorator(enabled = true, descriptor = "@ "),
                        ),
                        WordScheme(
                            words = DefaultWordList.WORD_LIST_MAP["Lorem"]!!.words,
                            capitalization = CapitalizationMode.LOWER,
                            arrayDecorator = ArrayDecorator(
                                enabled = true,
                                minCount = 3,
                                maxCount = 7,
                                separator = " ",
                                affixDecorator = AffixDecorator(enabled = true, descriptor = "@."),
                            )
                        ),
                    )
                ),
                Template(
                    "IP address",
                    listOf(
                        IntegerScheme(
                            minValue = 0,
                            maxValue = 255,
                            arrayDecorator = ArrayDecorator(
                                enabled = true,
                                minCount = 4,
                                maxCount = 4,
                                separator = "."
                            ),
                        ),
                    )
                )
            )


        /**
         * Constructs a [TemplateList] consisting of a single template containing [schemes].
         *
         * @param schemes the schemes to give to the list's single template
         * @param name the name of the template
         */
        fun from(vararg schemes: Scheme, name: String = Bundle("template.name.default")) =
            TemplateList(listOf(Template(name, schemes.toList())))
    }
}
