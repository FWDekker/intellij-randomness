package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 *
 * @param <S> the type of settings that should be persisted
 */
interface Settings<S> : PersistentStateComponent<S> {
    /**
     * Returns a settings instance with default settings.
     *
     * @return a settings instance with default settings
     */
    fun newState(): S

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
