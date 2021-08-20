package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.annotations.Transient
import javax.swing.Icon
import kotlin.random.Random


/**
 * A scheme is a [State] that is also a configurable random number generator.
 *
 * Schemes can additionally be given [SchemeDecorator]s that extend their functionality.
 */
abstract class Scheme : State() {
    /**
     * Settings that determine whether the output should be an array of values.
     */
    abstract val decorator: ArraySchemeDecorator?

    /**
     * The icon for this scheme; depends on whether its array decorator is enabled.
     */
    override val icon: Icon?
        get() =
            if (decorator?.enabled == true) icons?.Array
            else icons?.Base


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
    fun generateStrings(count: Int = 1): List<String> {
        doValidate()?.also { throw DataGenerationException(it) }

        return decorator.let { decorator ->
            if (decorator == null) {
                generateUndecoratedStrings(count)
            } else {
                decorator.generator = ::generateUndecoratedStrings
                decorator.generateStrings(count)
            }
        }
    }

    /**
     * Generates random data according to the settings in this scheme, ignoring settings from decorators.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    abstract fun generateUndecoratedStrings(count: Int = 1): List<String>


    abstract override fun deepCopy(retainUuid: Boolean): Scheme
}

/**
 * Transparently extends or alters the functionality of a [Scheme] with a decorating function.
 */
abstract class SchemeDecorator : Scheme() {
    /**
     * The generating function that should be decorated.
     */
    @Transient
    var generator: (Int) -> List<String> = { emptyList() }
}

/**
 * Thrown if a random datum could not be generated.
 *
 * @param message the detail message
 * @param cause the cause
 */
class DataGenerationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
