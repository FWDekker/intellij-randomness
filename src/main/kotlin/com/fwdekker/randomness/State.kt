package com.fwdekker.randomness

import com.fasterxml.uuid.Generators
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * A state holds variables that can be configured, validated, copied, and loaded.
 *
 * All non-transient non-primitive fields of a state should be immutable references. For example, if a state has a field
 * `list: List<String>`, then `list` should be a `val`, not a `var`, so that references to `state.list` remain valid
 * even after `copyFrom`.
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
    var context: Box<Settings> = Box({ Settings.DEFAULT })
        protected set


    /**
     * Sets the [State.context] of this [State] to be a reference to [context].
     */
    fun applyContext(context: Settings) = applyContext(Box({ context }))

    /**
     * Sets the [State.context] of this [State] to [context].
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
     * Returns a deep copy, retaining the [uuid] if and only if [retainUuid] is `true`.
     *
     * Fields annotated with [Transient] are shallow-copied.
     *
     * @see deepCopyTransient utility function for subclasses that want to implement `deepCopy`
     */
    abstract fun deepCopy(retainUuid: Boolean = false): State

    /**
     * When invoked by the instance `this` on (another) instance `self` as `self.deepCopyTransient()`, this method
     * copies [Transient] fields from `this` to `self`, and returns `self`.
     *
     * @see deepCopy
     */
    protected fun <SELF : State> SELF.deepCopyTransient(retainUuid: Boolean): SELF {
        val self: SELF = this
        val thiz: State = this@State

        if (retainUuid) self.uuid = thiz.uuid
        self.applyContext(thiz.context.copy())

        return self
    }

    /**
     * Copies [other] into `this`.
     *
     * Works by shallow-copying a [deepCopy] of [other] into `this`. [Transient] fields are shallow-copied directly from
     * [other] instead.
     *
     * Implementations may choose to shallow-copy additional fields directly from [other].
     *
     * @param other the state to copy into `this`; should be a (sub)class of this state
     * @see copyFromTransient utility function for subclasses that want to implement `copyFrom`
     */
    open fun copyFrom(other: State) {
        XmlSerializerUtil.copyBean(other.deepCopy(retainUuid = true), this)
        copyFromTransient(other)
    }

    /**
     * Copies basic [Transient] fields from [other] into `this`.
     *
     * Typically used by subclasses to help implement [copyFrom].
     */
    protected fun copyFromTransient(other: State) {
        this.uuid = other.uuid
        this.applyContext(other.context.copy())
    }
}
