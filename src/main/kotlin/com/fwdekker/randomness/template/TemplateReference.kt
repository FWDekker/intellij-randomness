package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.OverlayIcon
import com.fwdekker.randomness.OverlayedIcon
import com.fwdekker.randomness.RandomnessIcons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SchemeDecorator
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.State
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.intellij.util.xmlb.annotations.Transient
import java.awt.Color


/**
 * A reference to an existing template.
 *
 * @property templateUuid The UUID of the referenced template; defaults to the first template in [TemplateList].
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class TemplateReference(
    var templateUuid: String? = null,
    var arrayDecorator: ArrayDecorator = ArrayDecorator()
) : Scheme() {
    override val name: String
        get() = template?.name?.let { "[$it]" } ?: Bundle("reference.title")

    override val typeIcon: TypeIcon
        get() = template?.typeIcon ?: DEFAULT_ICON

    override val icon: OverlayedIcon
        get() = OverlayedIcon(typeIcon, decorators.mapNotNull { it.icon } + OverlayIcon.REFERENCE)

    override val decorators: List<SchemeDecorator>
        get() = listOf(arrayDecorator)

    /**
     * The list in which this reference _must_ reside, and in which the referenced template _might_ reside.
     *
     * Using a [Box] to prevent recursive initialization.
     */
    @get:Transient
    var templateList: Box<TemplateList> = Box({ TemplateSettings.default.state })

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
        template?.also { it.random = random }?.generateStrings(count)
            ?: throw DataGenerationException(Bundle("reference.error.generate_null"))

    override fun setSettingsState(settingsState: SettingsState) {
        super.setSettingsState(settingsState)
        templateList += settingsState.templateList
    }


    override fun doValidate(): String? {
        val recursion = (+templateList).findRecursionFrom(this)

        return if (templateUuid == null)
            Bundle("reference.error.no_selection")
        else if (template == null)
            Bundle("reference.error.not_found")
        else if (recursion != null)
            Bundle("reference.error.recursion", "(${recursion.joinToString(separator = " → ") { it.name }})")
        else
            arrayDecorator.doValidate()
    }

    override fun copyFrom(other: State) {
        require(other is TemplateReference) { Bundle("shared.error.cannot_copy_from_different_type") }

        super.copyFrom(other)
        templateList = other.templateList.copy()
    }

    override fun deepCopy(retainUuid: Boolean) =
        TemplateReference(
            templateUuid = templateUuid,
            arrayDecorator = arrayDecorator.deepCopy(retainUuid)
        ).also {
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
        val DEFAULT_ICON = TypeIcon(RandomnessIcons.TEMPLATE, "", listOf(Color(110, 110, 110)))
    }
}
