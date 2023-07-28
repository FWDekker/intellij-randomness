package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.StateContext
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
     * The list in which this reference _must_ reside, and in which the referenced template _might_ reside.
     *
     * Using a [Box] to prevent recursive initialization.
     */
    @get:Transient
    var templateList: Box<TemplateList> = Box({ TemplateListSettingsComponent.default.state })

    /**
     * The template in [templateList] that contains this reference.
     */
    val parent: Template
        get() = (+templateList).templates.single { template -> template.schemes.any { it.uuid == uuid } }

    /**
     * The template that is being referenced, or `null` if it could not be found in [templateList].
     */
    @get:Transient
    var template: Template?
        get() = (+templateList).templates.singleOrNull { it.uuid == templateUuid }
        set(value) {
            templateUuid = value?.uuid
        }


    override fun generateUndecoratedStrings(count: Int) =
        (this.template ?: throw DataGenerationException(Bundle("reference.error.generate_null")))
            .also { it.random = random }
            .generateStrings(count)
            .map { capitalization.transform(it, random) }

    override fun setStateContext(stateContext: StateContext) {
        super.setStateContext(stateContext)
        templateList += stateContext.templateList
    }


    override fun doValidate(): String? {
        val recursion = (+templateList).findRecursionFrom(this)

        return when {
            templateUuid == null -> Bundle("reference.error.no_selection")
            template == null -> Bundle("reference.error.not_found")
            recursion != null -> Bundle("reference.error.recursion", "(${recursion.joinToString(" → ") { it.name }})")
            else -> arrayDecorator.doValidate()
        }
    }

    override fun copyFrom(other: State) {
        require(other is TemplateReference) { Bundle("shared.error.cannot_copy_from_different_type") }

        super.copyFrom(other)
        templateList = other.templateList.copy()
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(arrayDecorator = arrayDecorator.deepCopy(retainUuid))
            .also {
                if (retainUuid) it.uuid = uuid

                it.templateList = templateList.copy()
            }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The base icon for references when the reference is invalid.
         */
        val DEFAULT_ICON = TypeIcon(Icons.TEMPLATE, "", listOf(Gray._110))

        /**
         * The default value of the [capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN

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
