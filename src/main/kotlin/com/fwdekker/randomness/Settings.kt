package com.fwdekker.randomness

import com.intellij.openapi.components.PersistentStateComponent


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 *
 * @param <S> the class of settings that should be persisted
 */
interface Settings<S> : PersistentStateComponent<S>
