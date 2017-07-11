package com.fwdekker.randomness;

import com.intellij.openapi.components.ApplicationComponent;


/**
 * Superclass for classes that will contain settings that should persist over IDE restarts.
 */
public abstract class Settings implements ApplicationComponent {
    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
