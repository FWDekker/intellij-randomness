package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
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
    val templates: MutableList<Template> = DEFAULT_TEMPLATES,
) : State() {
    override fun applyContext(context: Box<Settings>) {
        super.applyContext(context)
        templates.forEach { it.applyContext(context) }
    }


    /**
     * Returns the template in this list with [uuid] as its UUID.
     */
    fun getTemplateByUuid(uuid: String) = templates.singleOrNull { it.uuid == uuid }

    /**
     * Returns the template or scheme in this list with [uuid] as its UUID.
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
        copy(templates = templates.map { it.deepCopy(retainUuid) }.toMutableList()).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates] field.
         */
        val DEFAULT_TEMPLATES
            get() = mutableListOf(
                Template("Integer", mutableListOf(IntegerScheme())),
                Template("Decimal", mutableListOf(DecimalScheme())),
                Template("String", mutableListOf(StringScheme())),
                Template("UUID", mutableListOf(UuidScheme())),
                Template("Date-Time", mutableListOf(DateTimeScheme())),
                Template("Hex color", mutableListOf(StringScheme(pattern = "#[0-9a-f]{6}"))),
                Template(
                    "Name",
                    mutableListOf(
                        WordScheme(
                            words = DefaultWordList.WORD_LIST_MAP["Forenames"]!!.words,
                            affixDecorator = AffixDecorator(enabled = true, descriptor = "@ "),
                        ),
                        WordScheme(words = DefaultWordList.WORD_LIST_MAP["Surnames"]!!.words),
                    )
                ),
                Template(
                    "Lorem Ipsum",
                    mutableListOf(
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
                    mutableListOf(
                        IntegerScheme(
                            minValue = 0,
                            maxValue = 255,
                            arrayDecorator = ArrayDecorator(
                                enabled = true,
                                minCount = 4,
                                maxCount = 4,
                                separator = ".",
                            ),
                        ),
                    )
                )
            )
    }
}
