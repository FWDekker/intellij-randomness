package com.fwdekker.randomness

import com.fwdekker.randomness.array.ArraySchemeDecorator
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlin.random.Random


/**
 * An object holding some form of state which can be edited.
 *
 * @param SELF the type of state that is stored; should be a self-reference
 */
interface State<SELF : State<SELF>> {
    /**
     * Validates the state, and indicates whether and why it is invalid.
     *
     * @return `null` if the state is valid, or a string explaining why the state is invalid
     */
    fun doValidate(): String? = null

    /**
     * Copies the fields of `state` into `this`.
     *
     * @param state the fields to load into `this`
     */
    fun loadState(state: SELF)

    /**
     * Returns a deep copy of itself.
     *
     * @return a deep copy of itself
     */
    fun deepCopy(): State<SELF>
}

/**
 * Configurable persistent settings.
 *
 * @param SELF the type of settings that is stored; should be a self-reference
 */
abstract class Settings<SELF : Settings<SELF>> : PersistentStateComponent<SELF>, State<SELF> {
    /**
     * Returns `this`.
     *
     * @return `this`
     */
    abstract override fun getState(): SELF

    override fun loadState(state: SELF) = XmlSerializerUtil.copyBean(state, this)

    abstract override fun deepCopy(): Settings<SELF>
}

/**
 * A scheme is a configurable random number generator.
 *
 * Schemes can additionally be given [SchemeDecorator]s that extend their functionality.
 *
 * @param SELF the type of scheme that is stored; should be a self-reference
 */
abstract class Scheme<SELF : Scheme<SELF>> : State<SELF> {
    /**
     * Settings that determine whether the output should be an array of values.
     */
    abstract var arrayDecorator: ArraySchemeDecorator

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
     * @see SchemeDecorator.decorate
     */
    @Throws(DataGenerationException::class)
    fun generateStrings(count: Int) = arrayDecorator.decorate(::generateUndecoratedStrings, count)

    /**
     * Generates random data according to the settings in this scheme, ignoring settings from decorators.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    abstract fun generateUndecoratedStrings(count: Int = 1): List<String>


    override fun loadState(state: SELF) = XmlSerializerUtil.copyBean(state, this)

    abstract override fun deepCopy(): Scheme<SELF>
}

/**
 * Transparently extends the functionality of a [Scheme] with a decorating function.
 *
 * @param SELF the type of decorator; should be a self-reference
 */
interface SchemeDecorator<SELF : SchemeDecorator<SELF>> : State<SELF> {
    /**
     * Decorates the given string generator function according to this scheme decorator's behavior.
     *
     * @param generator the string generator to decorate; this is usually a reference to
     * [Scheme.generateUndecoratedStrings]
     * @param count the number of decorated strings to generate
     * @return decorated strings
     * @see Scheme.generateStrings
     */
    fun decorate(generator: (Int) -> List<String>, count: Int = 1): List<String>
}
