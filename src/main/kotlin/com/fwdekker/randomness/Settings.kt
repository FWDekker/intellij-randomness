package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 *
 * @param <S> the type of settings that should be persisted
 */
interface Settings<S> : PersistentStateComponent<S> {
    /**
     * Returns `this`.
     *
     * @return `this`
     */
    override fun getState(): S

    /**
     * Copies the fields of `state` to `this`.
     *
     * @param state the state to load into `this`
     */
    override fun loadState(state: S)
}
