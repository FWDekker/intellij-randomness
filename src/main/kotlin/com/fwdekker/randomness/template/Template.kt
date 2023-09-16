package com.fwdekker.randomness.template

import com.fwdekker.randomness.Box
import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.TypeIcon
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.datetime.DateTimeScheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.ui.Gray
import com.intellij.util.xmlb.annotations.XCollection


/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property name The unique name of the template.
 * @property schemes The ordered list of underlying schemes.
 * @property arrayDecorator Settings that determine whether the output should be an array of values.
 */
data class Template(
    override var name: String = DEFAULT_NAME,
    @get:XCollection(
        elementTypes = [
            DateTimeScheme::class,
            DecimalScheme::class,
            IntegerScheme::class,
            StringScheme::class,
            TemplateReference::class,
            UuidScheme::class,
            WordScheme::class,
        ]
    )
    var schemes: MutableList<Scheme> = DEFAULT_SCHEMES,
    val arrayDecorator: ArrayDecorator = DEFAULT_ARRAY_DECORATOR,
) : Scheme() {
    override val typeIcon
        get() = schemes.mapNotNull { it.typeIcon }.reduceOrNull { acc, icon -> acc.combineWith(icon) } ?: DEFAULT_ICON
    override val decorators get() = listOf(arrayDecorator)

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
     * Generates random strings by concatenating the outputs of the [schemes].
     *
     * The schemes are first all given a reference to the same [random] before each generating [count] random strings.
     * These results are then concatenated into the output.
     *
     * @param count the number of random strings to generate
     * @return random strings generated from this template's underlying schemes
     */
    override fun generateUndecoratedStrings(count: Int) =
        schemes.onEach { it.random = random }.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { i -> data.joinToString("") { it[i] } } }


    override fun doValidate() =
        if (name.isBlank()) Bundle("template.error.no_name", Bundle("template.name.empty"))
        else schemes.firstNotNullOfOrNull { scheme -> scheme.doValidate()?.let { "${scheme.name} > $it" } }
            ?: arrayDecorator.doValidate()

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            schemes = schemes.map { it.deepCopy(retainUuid) }.toMutableList(),
            arrayDecorator = arrayDecorator.deepCopy(retainUuid),
        ).deepCopyTransient(retainUuid)


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

        /**
         * The default value of the [arrayDecorator] field.
         */
        val DEFAULT_ARRAY_DECORATOR get() = ArrayDecorator()
    }
}
