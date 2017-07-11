package com.fwdekker.randomness;

import com.intellij.openapi.components.ApplicationComponent;


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 */
public abstract class Settings implements ApplicationComponent {
    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract") // This method should not be overridden
    public final void initComponent() {
        // No interaction with other plugins
    }

    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract") // This method should not be overridden
    public final void disposeComponent() {
        // No interaction with other plugins
    }

    @Override
    public final String getComponentName() {
        return getClass().getSimpleName();
    }
}
