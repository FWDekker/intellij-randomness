package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import icons.RandomnessIcons
import javax.swing.Icon
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
    abstract val decorator: ArraySchemeDecorator?

    /**
     * The name of the scheme as shown to the user.
     */
    abstract val name: String

    /**
     * The icons that represent schemes of this type.
     */
    @Transient
    open val icons: RandomnessIcons? = null

    /**
     * The icon that represents this scheme instance.
     */
    @get:Transient
    open val icon: Icon?
        get() = if (decorator?.enabled == true) icons?.Array
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


    /**
     * Validates the scheme, and indicates whether and why it is invalid.
     *
     * @return `null` if the scheme is valid, or a string explaining why the scheme is invalid
     */
    open fun doValidate(): String? = null

    /**
     * Copies the given scheme into this scheme.
     *
     * Works by copying all references in a [deepCopy] of [scheme] into `this`. Note that fields marked with [Transient]
     * will be shallow-copied.
     *
     * @param scheme the scheme to copy into this scheme; should be a subclass of this scheme
     */
    fun copyFrom(scheme: Scheme) = XmlSerializerUtil.copyBean(scheme.deepCopy(), this)

    /**
     * Returns a deep copy of this scheme.
     *
     * Fields marked with [Transient] will be shallow-copied.
     *
     * @return a deep copy of this scheme
     */
    abstract fun deepCopy(): Scheme


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [name] field.
         */
        const val DEFAULT_NAME: String = "Unnamed scheme"
    }
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
