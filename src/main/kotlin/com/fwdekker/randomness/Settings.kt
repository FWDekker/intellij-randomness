package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 *
 * @param <S> the type of settings that should be persisted
 */
interface Settings<S> : PersistentStateComponent<S>
