package com.fwdekker.randomness;

import com.intellij.openapi.components.PersistentStateComponent;


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 *
 * @param <S> the class of settings that should be persisted
 */
public interface Settings<S extends Settings> extends PersistentStateComponent<S> {
}
