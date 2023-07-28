package com.fwdekker.randomness.template

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.Icons
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.StateContext
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
    override var name: String = Bundle("template.name.default"),
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
    var schemes: List<Scheme> = DEFAULT_SCHEMES.toMutableList(),
    var arrayDecorator: ArrayDecorator = ArrayDecorator(),
) : Scheme() {
    override val typeIcon
        get() = schemes.mapNotNull { it.typeIcon }.reduceOrNull { acc, icon -> acc.combineWith(icon) } ?: DEFAULT_ICON
    override val decorators get() = listOf(arrayDecorator)

    /**
     * The identifier of the action that inserts this [Template].
     */
    val actionId get() = "com.fwdekker.randomness.insert.${uuid.replace("-", "")}"


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
            schemes = schemes.map { it.deepCopy(retainUuid) },
            arrayDecorator = arrayDecorator.deepCopy(retainUuid),
        ).also { if (retainUuid) it.uuid = this.uuid }


    override fun setStateContext(stateContext: StateContext) {
        super.setStateContext(stateContext)
        schemes.forEach { it.setStateContext(stateContext) }
    }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The icon displayed when a template has no schemes.
         */
        val DEFAULT_ICON = TypeIcon(Icons.TEMPLATE, "", listOf(Gray._110))

        /**
         * The default value of the [schemes] field.
         */
        val DEFAULT_SCHEMES: List<Scheme>
            get() = emptyList()
    }
}
