package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.ui.Gray
import com.intellij.util.xmlb.annotations.Transient


/**
 * A reference to an existing template, to allow using a template as if it were a scheme.
 *
 * @property templateUuid The UUID of the referenced template; defaults to the first template in [TemplateList].
 * @property capitalization The way in which the generated word should be capitalized.
 * @property affixDecorator The affixation to apply to the generated values.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class TemplateReference(
    var templateUuid: String? = null,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var affixDecorator: AffixDecorator = DEFAULT_AFFIX_DECORATOR,
    var arrayDecorator: ArrayDecorator = DEFAULT_ARRAY_DECORATOR,
) : Scheme() {
    override val name get() = template?.name?.let { "[$it]" } ?: Bundle("reference.title")
    override val typeIcon get() = template?.typeIcon ?: DEFAULT_ICON
    override val icon get() = OverlayedIcon(typeIcon, decorators.mapNotNull { it.icon } + OverlayIcon.REFERENCE)
    override val decorators get() = listOf(arrayDecorator)

    /**
     * The [Template] in the [context]'s [TemplateList] that contains this [TemplateReference].
     */
    val parent: Template
        get() = (+context).templates.single { template -> template.schemes.any { it.uuid == uuid } }

    /**
     * The [Template] that is being referenced, or `null` if it could not be found in the [context]'s [TemplateList].
     */
    @get:Transient
    var template: Template?
        get() = (+context).templates.singleOrNull { it.uuid == templateUuid }
        set(value) {
            templateUuid = value?.uuid
        }


    override fun generateUndecoratedStrings(count: Int) =
        (this.template ?: throw DataGenerationException(Bundle("reference.error.generate_null")))
            .also { it.random = random }
            .generateStrings(count)
            .map { capitalization.transform(it, random) }


    override fun doValidate(): String? {
        val recursion = (+context).templateList.findRecursionFrom(this)

        return when {
            templateUuid == null -> Bundle("reference.error.no_selection")
            template == null -> Bundle("reference.error.not_found")
            recursion != null -> Bundle("reference.error.recursion", "(${recursion.joinToString(" → ") { it.name }})")
            else -> arrayDecorator.doValidate()
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid)).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for references when the reference is invalid.
         */
        val DEFAULT_ICON = TypeIcon(Icons.TEMPLATE, "", listOf(Gray._110))

        /**
         * The preset values for the [capitalization] field.
         */
        val PRESET_CAPITALIZATION = arrayOf(
            CapitalizationMode.RETAIN,
            CapitalizationMode.LOWER,
            CapitalizationMode.UPPER,
            CapitalizationMode.RANDOM,
            CapitalizationMode.SENTENCE,
            CapitalizationMode.FIRST_LETTER,
        )

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

        /**
         * The preset values for the [affixDecorator] descriptor.
         */
        val PRESET_AFFIX_DECORATOR_DESCRIPTORS = listOf("'", "\"", "`")

        /**
         * The default value of the [affixDecorator] field.
         */
        val DEFAULT_AFFIX_DECORATOR = AffixDecorator(enabled = false, descriptor = "\"")

        /**
         * The default value of the [arrayDecorator] field.
         */
        val DEFAULT_ARRAY_DECORATOR = ArrayDecorator()
    }
}
