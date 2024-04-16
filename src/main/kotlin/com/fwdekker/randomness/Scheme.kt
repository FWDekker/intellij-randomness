package com.fwdekker.randomness

import com.fwdekker.randomness.affix.AffixDecorator
import com.fwdekker.randomness.array.ArrayDecorator
import com.fwdekker.randomness.fixedlength.FixedLengthDecorator
import com.intellij.util.xmlb.annotations.Transient
import com.intellij.util.xmlb.annotations.XCollection
import kotlin.random.Random


/**
 * A scheme is a [State] that is also a configurable random number generator.
 *
 * Schemes may use [DecoratorScheme]s to extend their functionality.
 */
abstract class Scheme : State() {
    /**
     * The name of the scheme as shown to the user.
     */
    abstract val name: String

    /**
     * The icon signifying the type of data represented by this scheme, ignoring its [decorators], or `null` if this
     * scheme does not represent any kind of data, as is the case for [DecoratorScheme]s.
     */
    @get:Transient
    open val typeIcon: TypeIcon? = null

    /**
     * The icon signifying this scheme in its entirety, or `null` if it does not have an icon.
     */
    @get:Transient
    open val icon: OverlayedIcon? get() = typeIcon?.let { OverlayedIcon(it, decorators.mapNotNull(Scheme::icon)) }

    /**
     * Additional logic that determines how strings are generated.
     *
     * Decorators are automatically applied when [generateStrings] is invoked. To generate strings without using
     * decorators, use [generateUndecoratedStrings]. Decorators are applied in ascending order. That is, the output of
     * the scheme is fed into the decorator at index `0`, and that output is fed into the decorator at index `1`, and so
     * on.
     */
    @get:Transient
    @get:XCollection(elementTypes = [AffixDecorator::class, ArrayDecorator::class, FixedLengthDecorator::class])
    abstract val decorators: List<DecoratorScheme>

    /**
     * The random number generator used to generate random values.
     */
    @get:Transient
    var random: Random = Random.Default


    override fun applyContext(context: Box<Settings>) {
        super.applyContext(context)
        decorators.forEach { it.applyContext(context) }
    }


    /**
     * Generates [count] random decorated data according to the settings in this scheme and its decorators.
     *
     * By default, this method applies the decorators on the output of [generateUndecoratedStrings]. Override this
     * method if the scheme should interact with its decorators in a different way.
     *
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    open fun generateStrings(count: Int = 1): List<String> {
        doValidate()?.also { throw DataGenerationException(it) }

        return decorators
            .fold(this::generateUndecoratedStrings) { previousGenerator, currentScheme ->
                currentScheme.random = random
                currentScheme.generator = previousGenerator
                currentScheme::generateStrings
            }
            .invoke(count)
    }

    /**
     * Generates [count] random data according to the settings in this scheme, ignoring settings from decorators.
     *
     * @throws DataGenerationException if data could not be generated
     * @see generateStrings
     */
    @Throws(DataGenerationException::class)
    protected abstract fun generateUndecoratedStrings(count: Int = 1): List<String>


    abstract override fun deepCopy(retainUuid: Boolean): Scheme
}

/**
 * Transparently extends or alters the functionality of a [Scheme] with a decorating function.
 *
 * Requires that [generator] is set before invoking [generateStrings].
 */
@Suppress("detekt:LateinitUsage") // Alternatives not feasible
abstract class DecoratorScheme : Scheme() {
    /**
     * Whether this decorator is enabled, or whether any invocation of [generateStrings] should be passed directly to
     * the [generator].
     */
    protected open val isEnabled: Boolean = true

    /**
     * The generating function whose output should be decorated.
     */
    @get:Transient
    lateinit var generator: (Int) -> List<String>


    override fun generateStrings(count: Int): List<String> {
        doValidate()?.also { throw DataGenerationException(it) }

        return if (isEnabled) super.generateStrings(count)
        else generator(count)
    }


    abstract override fun deepCopy(retainUuid: Boolean): DecoratorScheme
}


/**
 * Thrown if a random datum could not be generated.
 *
 * @param message the detail message
 * @param cause the cause
 */
class DataGenerationException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
