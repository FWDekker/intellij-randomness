package com.fwdekker.randomness

import com.fasterxml.uuid.Generators
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * A state holds variables that can be configured.
 */
abstract class State {
    /**
     * A UUID to uniquely track this scheme even when it is copied.
     */
    var uuid: String = Generators.randomBasedGenerator(Random.Default.asJavaRandom()).generate().toString()


    /**
     * Validates the state, and indicates whether and why it is invalid.
     *
     * @return `null` if the state is valid, or a string explaining why the state is invalid
     */
    open fun doValidate(): String? = null

    /**
     * Copies the given state into this state.
     *
     * Works by copying all references in a [deepCopy] of [state] into `this`. Note that fields marked with [Transient]
     * will be shallow-copied.
     *
     * @param state the state to copy into this state; should be a (sub)class of this state
     */
    open fun copyFrom(state: State) = XmlSerializerUtil.copyBean(state.deepCopy(retainUuid = true), this)

    /**
     * Returns a deep copy of this state.
     *
     * Fields marked with [Transient] will be shallow-copied.
     *
     * @param retainUuid false if and only if the copy should have a different [uuid]
     * @return a deep copy of this scheme
     */
    abstract fun deepCopy(retainUuid: Boolean = false): State
}
