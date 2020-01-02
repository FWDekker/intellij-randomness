package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.xmlb.annotations.Transient


/**
 * Settings are composed of [Scheme]s and persist these over IDE restarts.
 *
 * @param SELF the type of settings that should be persisted; should be a self reference
 * @param SCHEME the type of scheme that the settings consist of
 */
interface Settings<SELF, SCHEME : Scheme<SCHEME>> : PersistentStateComponent<SELF> {
    /**
     * The various schemes that are contained within the settings.
     */
    var schemes: MutableList<SCHEME>
    /**
     * The name of the scheme that is currently active.
     */
    var currentSchemeName: String


    /**
     * The instance of the scheme that is currently active.
     *
     * This field is backed by [currentSchemeName]. If [currentSchemeName] refers to a scheme that is not contained in
     * [schemes], `get`ting this field will throw an exception.
     */
    var currentScheme: SCHEME
        @Transient
        get() = schemes.first { it.name == currentSchemeName }
        set(value) {
            currentSchemeName = value.name
        }


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


/**
 * A scheme is a collection of configurable values.
 *
 * In a typical use case a user can quickly switch between instances of schemes of the same type to change the "preset"
 * or "configuration" that is currently being used.
 *
 * @param SELF the type of scheme that is stored; should be a self reference
 * @see Settings
 */
interface Scheme<SELF> : com.intellij.openapi.options.Scheme {
    companion object {
        /**
         * The name of the default scheme.
         */
        const val DEFAULT_NAME = "Default"
    }


    /**
     * The name of the scheme, used to identify it.
     */
    var myName: String


    /**
     * Same as [myName].
     */
    override fun getName() = myName


    /**
     * Shallowly copies the state of [other] into `this`.
     *
     * @param other the state to copy into `this`
     */
    fun copyFrom(other: SELF)

    /**
     * Returns a copy of this scheme that has the given name.
     *
     * @param name the name to give to the copy
     * @return a copy of this scheme that has the given name
     */
    fun copyAs(name: String): SELF
}
