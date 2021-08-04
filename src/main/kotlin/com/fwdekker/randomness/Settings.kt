package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent


// TODO: Overhaul settings to be specific to UDS? But also leave space for symbol sets!
/**
 * Settings are composed of [Scheme]s and persist these over IDE restarts.
 *
 * @param SELF the type of settings that should be persisted; should be a self reference
 */
interface Settings<SELF> : PersistentStateComponent<SELF> {
    /**
     * Returns a deep copy of the settings and the contained schemes.
     *
     * @return a deep copy of the settings and the contained schemes
     */
    fun deepCopy(): SELF

    /**
     * Returns `this`.
     *
     * @return `this`
     */
    override fun getState(): SELF

    /**
     * Copies the fields of `state` to `this`.
     *
     * @param state the state to load into `this`
     */
    override fun loadState(state: SELF)
}


// TODO: Move validation into the scheme (but maybe separate run-time validation from creation validation?)
/**
 * A scheme is a collection of configurable values.
 *
 * @param SELF the type of scheme that is stored; should be a self-reference
 * @see Settings
 */
interface Scheme<SELF> {
    /**
     * Generates random data according to the settings in this scheme.
     *
     * @param count the number of data to generate
     * @return random data
     * @throws DataGenerationException if data could not be generated
     */
    @Throws(DataGenerationException::class)
    fun generateStrings(count: Int = 1): List<String>

    /**
     * Shallowly copies the state of [other] into `this`.
     *
     * @param other the state to copy into `this`
     */
    fun copyFrom(other: SELF)
}
