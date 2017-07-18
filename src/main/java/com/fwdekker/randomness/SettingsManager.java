package com.fwdekker.randomness;

import org.jetbrains.annotations.NotNull;


/**
 * A {@code SettingsManager} is an object that can change settings of a given type.
 *
 * @param <S> the type of settings that are changed by the implementation
 */
public interface SettingsManager<S extends Settings> {
    /**
     * Loads the default settings instance into the implementing object.
     */
    void loadSettings();

    /**
     * Loads the given settings instance into the implementing object.
     *
     * @param settings a settings instance
     */
    void loadSettings(@NotNull S settings);

    /**
     * Saves the implementing object's state into the default settings instance.
     */
    void saveSettings();

    /**
     * Saves the implementing object's state into the given settings instance.
     *
     * @param settings a settings instance
     */
    void saveSettings(@NotNull S settings);
}
