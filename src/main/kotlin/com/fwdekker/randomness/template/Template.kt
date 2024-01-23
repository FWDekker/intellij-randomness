package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.DecoratorScheme
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.TypeIcon
import com.intellij.ui.Gray
import kotlin.random.Random


/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property name The unique name of the template.
 * @property schemes The ordered list of underlying schemes.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class Template(
    override var name: String = DEFAULT_NAME,
    val schemes: MutableList<Scheme> = DEFAULT_SCHEMES,
) : Scheme() {
    override val typeIcon
        get() = TypeIcon.combine(schemes.mapNotNull { it.typeIcon }) ?: DEFAULT_ICON
    override val decorators get() = emptyList<DecoratorScheme>()

    /**
     * The identifier of the action that inserts this [Template].
     */
    val actionId get() = "com.fwdekker.randomness.insert.${uuid.replace("-", "")}"


    override fun applyContext(context: Box<Settings>) {
        super.applyContext(context)
        schemes.forEach { it.applyContext(context) }
    }


    /**
     * Returns `true` if `this` template can add a [TemplateReference] that refers to [target] without causing recursion
     * within the current [context].
     */
    fun canReference(target: Template): Boolean {
        val tempRef = TemplateReference().also { it.applyContext(context) }

        return 0
            .also { schemes += tempRef }
            .let { tempRef.canReference(target) }
            .also { schemes -= tempRef }
    }


    /**
     * Generates [count] random strings by concatenating the outputs of the [schemes].
     *
     * The schemes are first all given a reference to the same [random] before each generating [count] random strings.
     * These results are then concatenated into the output.
     */
    override fun generateUndecoratedStrings(count: Int): List<String> {
        val seed = random.nextInt()

        return schemes.onEach { it.random = Random(seed + it.uuid.hashCode()) }
            .map { it.generateStrings(count) }
            .let { data -> (0 until count).map { i -> data.joinToString("") { it[i] } } }
    }


    override fun doValidate() =
        if (name.isBlank()) Bundle("template.error.no_name", Bundle("template.name.empty"))
        else schemes.firstNotNullOfOrNull { scheme -> scheme.doValidate()?.let { "${scheme.name} > $it" } }

    override fun deepCopy(retainUuid: Boolean) =
        copy(schemes = schemes.map { it.deepCopy(retainUuid) }.toMutableList(),).deepCopyTransient(retainUuid)


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The icon displayed when a template has no schemes.
         */
        val DEFAULT_ICON = TypeIcon(Icons.TEMPLATE, "", listOf(Gray._110))

        /**
         * The default value of the [name] field.
         */
        val DEFAULT_NAME = Bundle("template.name.default")

        /**
         * The default value of the [schemes] field.
         */
        val DEFAULT_SCHEMES get() = mutableListOf<Scheme>()
    }
}
