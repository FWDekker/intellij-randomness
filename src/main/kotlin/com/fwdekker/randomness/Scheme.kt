package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.fixedlength.FixedLengthDecorator
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.util.xmlb.annotations.XCollection
import kotlin.random.Random


/**
 * A scheme is a [State] that is also a configurable random number generator.
 *
 * Schemes can additionally be given [SchemeDecorator]s that extend their functionality.
 */
abstract class Scheme : State() {
    /**
     * The name of the scheme as shown to the user.
     */
    abstract val name: String

    /**
     * The icon signifying the type of data represented by this scheme, ignoring its [decorators], or `null` if this
     * scheme does not represent any kind of data, as is the case for [SchemeDecorator]s.
     */
    @get:Transient
    open val typeIcon: TypeIcon?
        get() = null

    /**
     * The icon signifying this scheme in its entirety, or `null` if it does not have an icon.
     */
    @get:Transient
    open val icon: OverlayedIcon?
        get() = typeIcon?.let { OverlayedIcon(it, decorators.mapNotNull(Scheme::icon)) }

    /**
     * Additional logic that determines how strings are generated.
     *
     * Decorators are automatically applied when [generateStrings] is invoked. To generate strings without using
     * decorators, use [generateUndecoratedStrings]. Decorators are applied in ascending order. That is, the output of
     * the scheme is fed into the decorator at index `0`, and that output is fed into the decorator at index `1`, and so
     * on.
     */
    @get:Transient
    @get:XCollection(elementTypes = [ArrayDecorator::class, FixedLengthDecorator::class])
    abstract val decorators: List<SchemeDecorator>


    /**
     * The random number generator used to generate random values.
     */
    @get:Transient
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

        val generators = listOf(this) + decorators
        decorators.forEachIndexed { i, decorator ->
            decorator.random = random
            decorator.generator = generators[i]::generateUndecoratedStrings
        }

        return generators.last().generateUndecoratedStrings(count)
    }

    /**
     * Generates random data according to the settings in this scheme, ignoring settings from decorators.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     * @see generateStrings
     */
    @Throws(DataGenerationException::class)
    abstract fun generateUndecoratedStrings(count: Int = 1): List<String>

    /**
     * Sets the [SettingsState] that may be used by this scheme.
     *
     * Useful in case the scheme's behavior depends not only on its own internal state, but also that of other settings.
     */
    open fun setSettingsState(settingsState: SettingsState) {
        decorators.forEach { it.setSettingsState(settingsState) }
    }


    abstract override fun deepCopy(retainUuid: Boolean): Scheme
}

/**
 * Transparently extends or alters the functionality of a [Scheme] with a decorating function.
 */
abstract class SchemeDecorator : Scheme() {
    /**
     * The generating function that should be decorated.
     */
    @get:Transient
    var generator: (Int) -> List<String> = { emptyList() }


    abstract override fun deepCopy(retainUuid: Boolean): SchemeDecorator
}

/**
 * Thrown if a random datum could not be generated.
 *
 * @param message the detail message
 * @param cause the cause
 */
class DataGenerationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)