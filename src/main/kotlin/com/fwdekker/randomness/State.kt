package com.fwdekker.randomness

import com.fasterxml.uuid.Generators
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import java.util.UUID
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * A state holds variables that can be configured.
 */
abstract class State {
    /**
     * A UUID to uniquely track this scheme even when it is copied.
     */
    @get:Transient
    var uuid: UUID = Generators.randomBasedGenerator(Random.Default.asJavaRandom()).generate()


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
    open fun copyFrom(scheme: State) = XmlSerializerUtil.copyBean(scheme.deepCopy(retainUuid = true), this)

    /**
     * Returns a deep copy of this scheme.
     *
     * Fields marked with [Transient] will be shallow-copied.
     *
     * @param retainUuid false if and only if the copy should have a different [uuid]
     * @return a deep copy of this scheme
     */
    abstract fun deepCopy(retainUuid: Boolean = false): State


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
