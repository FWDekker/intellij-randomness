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
     * The context of this state in the form of a reference to the [Settings].
     *
     * Useful in case the scheme's behavior depends not only on its own internal state, but also on that of other
     * schemes.
     *
     * @see applyContext
     */
    @get:Transient
    var context: Box<Settings> = Box({ PersistentSettings.default.state })
        protected set


    /**
     * Sets the [context] of this [State].
     *
     * @param context the new context of this [State]
     */
    fun applyContext(context: Settings) = applyContext(Box({ context }))

    /**
     * @see applyContext
     */
    open fun applyContext(context: Box<Settings>) {
        this.context = context
    }


    /**
     * Validates the state, and indicates whether and why it is invalid.
     *
     * @return `null` if the state is valid, or a string explaining why the state is invalid
     */
    open fun doValidate(): String? = null

    /**
     * Returns a deep copy of this state.
     *
     * Fields annotated with [Transient] are shallow-copied.
     *
     * @param retainUuid `false` if and only if the copy should have a different, new [uuid]
     * @return a deep copy of this scheme
     * @see deepCopyTransient utility function for implementations
     */
    abstract fun deepCopy(retainUuid: Boolean = false): State

    /**
     * Copies [Transient] fields from `this` to `self`.
     *
     * @param SELF the type of `self`
     * @receiver `self`, the [State] that should copy [Transient] fields from `this` into itself
     * @param retainUuid `false` if and only if `self` should retain its current [uuid]
     * @return `self`
     * @see deepCopy
     */
    protected fun <SELF : State> SELF.deepCopyTransient(retainUuid: Boolean): SELF {
        val copy: SELF = this
        val original: State = this@State

        if (retainUuid) copy.uuid = original.uuid
        copy.applyContext(original.context.copy())

        return copy
    }

    /**
     * Copies the [other] into this state.
     *
     * Works by shallow-copying a [deepCopy] of [other] into `this`. [Transient] fields are shallow-copied directly from
     * [other] instead.
     *
     * Implementations may choose to shallow-copy additional fields directly from [other].
     *
     * @param other the state to copy into this state; should be a (sub)class of this state
     * @see copyFromTransient utility function for implementations
     */
    open fun copyFrom(other: State) {
        XmlSerializerUtil.copyBean(other.deepCopy(retainUuid = true), this)
        copyFromTransient(other)
    }

    /**
     * Copies [Transient] fields from [other] into `this`.
     *
     * @param other the state to copy [Transient] fields from into `this`
     */
    protected fun copyFromTransient(other: State) {
        this.uuid = other.uuid
        this.applyContext(other.context.copy())
    }
}
