package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.XCollection
import icons.RandomnessIcons


/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property name The unique name of the template.
 * @property schemes The ordered list of underlying schemes.
 * @property decorator Settings that determine whether the output should be an array of values.
 */
data class Template(
    override var name: String = DEFAULT_NAME,
    @get:XCollection(
        elementTypes = [
            IntegerScheme::class,
            DecimalScheme::class,
            StringScheme::class,
            WordScheme::class,
            UuidScheme::class,
            LiteralScheme::class
        ]
    )
    var schemes: List<Scheme> = DEFAULT_SCHEMES,
    override var decorator: ArraySchemeDecorator? = null
) : Scheme() {
    override val icons: RandomnessIcons
        get() = schemes.singleOrNull()?.icons ?: RandomnessIcons.Data


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
            .let { data -> (0 until count).map { i -> data.joinToString(separator = "") { it[i] } } }


    override fun doValidate() =
        if (name.isBlank()) "Templates must have a name."
        else schemes.firstNotNullOfOrNull { scheme -> scheme.doValidate()?.let { "${scheme.name} > $it" } }

    override fun deepCopy() = copy(schemes = schemes.map { it.deepCopy() })


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [name] field.
         */
        const val DEFAULT_NAME = "Unnamed template"

        /**
         * The default value of the [schemes] field.
         */
        val DEFAULT_SCHEMES: List<Scheme>
            get() = listOf(IntegerScheme())
    }
}
