package com.fwdekker.randomness

import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import kotlin.random.Random


/**
 * A scheme is a configurable random number generator.
 *
 * Schemes can additionally be given [SchemeDecorator]s that extend their functionality.
 */
abstract class Scheme {
    /**
     * Settings that determine whether the output should be an array of values.
     */
    abstract val decorator: SchemeDecorator?

    /**
     * The random number generator used to generate random values.
     */
    @Transient
    var random: Random = Random.Default


    /**
     * Generates random data according to the settings in this scheme, including settings from decorators.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    fun generateStrings(count: Int = 1): List<String> =
        decorator?.let {
            it.generator = ::generateUndecoratedStrings
            it.generateStrings(count)
        } ?: generateUndecoratedStrings(count)

    /**
     * Generates random data according to the settings in this scheme, ignoring settings from decorators.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    abstract fun generateUndecoratedStrings(count: Int = 1): List<String>


    /**
     * Validates the scheme, and indicates whether and why it is invalid.
     *
     * @return `null` if the scheme is valid, or a string explaining why the scheme is invalid
     */
    open fun doValidate(): String? = null

    /**
     * Copies the given scheme into this scheme.
     *
     * @param scheme the scheme to copy into this scheme; should be a subclass of this scheme
     */
    // TODO: Is this a deep copy?
    fun copyFrom(scheme: Scheme) = XmlSerializerUtil.copyBean(scheme, this)

    /**
     * Returns a deep copy of this scheme.
     *
     * @return a deep copy of this scheme
     */
    abstract fun deepCopy(): Scheme
}

/**
 * Transparently extends the functionality of a [Scheme] with a decorating function.
 */
abstract class SchemeDecorator : Scheme() {
    /**
     * The generating function that should be decorated.
     */
    @Transient
    var generator: (Int) -> List<String> = { emptyList() }
}
