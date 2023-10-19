package com.fwdekker.randomness

import com.fasterxml.uuid.Generators
import com.intellij.util.xmlb.annotations.Transient
import kotlin.random.Random
import kotlin.random.asJavaRandom


/**
 * A state holds variables that can be configured, validated, copied, and loaded.
 *
 * In a [State], fields are typically mutable, because the user can change the field. However, fields that themselves
 * also contain data (e.g. a [Collection] or a [DecoratorScheme]) should be stored as immutable references (but may
 * themselves be mutable). For example, instead of having the field `var list: List<String>`, a [State] should have the
 * field `val list: MutableList<String>`. This reflects the typical case in which the user does not change the reference
 * to the object, but changes the properties inside the object. And, more importantly, means that nested a
 * [SchemeEditor] can share a single unchanging reference to a [State] with its nested [SchemeEditor]s.
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
     * @see deepCopyTransient utility function for subclasses that want to implement [deepCopy]
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
        self.applyContext(thiz.context.copy()) // Copies the [Box], not the context itself

        return self
    }
}
