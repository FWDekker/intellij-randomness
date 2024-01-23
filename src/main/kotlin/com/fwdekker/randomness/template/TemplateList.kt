package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.State
import com.intellij.util.xmlb.annotations.OptionTag


/**
 * A collection of different templates.
 *
 * @property templates The collection of templates, each with a unique name.
 */
data class TemplateList(
    @OptionTag
    val templates: MutableList<Template> = DEFAULT_TEMPLATES,
) : State() {
    override fun applyContext(context: Box<Settings>) {
        super.applyContext(context)
        templates.forEach { it.applyContext(context) }
    }


    /**
     * Returns the template in this list that has [uuid] as its UUID, or `null` if there is no such template.
     */
    fun getTemplateByUuid(uuid: String) = templates.singleOrNull { it.uuid == uuid }

    /**
     * Returns the template or scheme in this list that has [uuid] as its UUID, or `null` if there is no such template
     * or scheme.
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

    /**
     * Note that the [context] must be updated manually.
     *
     * @see State.deepCopy
     */
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
            get() = mutableListOf<Template>()
    }
}
