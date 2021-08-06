package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.decimal.DecimalScheme
import com.fwdekker.randomness.integer.IntegerScheme
import com.fwdekker.randomness.literal.LiteralScheme
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.uuid.UuidScheme
import com.fwdekker.randomness.word.WordScheme
import com.intellij.util.xmlb.annotations.XCollection


/**
 * Generates random data by concatenating the random outputs of a list of [Scheme]s.
 *
 * @property schemes The ordered list of underlying schemes.
 */
data class Template(
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
    var schemes: List<Scheme<*>> = DEFAULT_SCHEMES
) : Scheme<Template>() {
    override fun generateStrings(count: Int) =
        schemes.onEach { it.random = random }.map { it.generateStrings(count) }
            .let { data -> (0 until count).map { i -> data.joinToString(separator = "") { it[i] } } }


    override fun deepCopy() =
        copy().also { copy -> copy.schemes = this.schemes.map { it.deepCopy() } }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [templates][templates] field.
         */
        val DEFAULT_SCHEMES: List<Scheme<*>>
            get() = listOf(IntegerScheme())
    }
}
