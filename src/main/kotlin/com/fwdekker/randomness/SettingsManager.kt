package com.fwdekker.randomness


/**
 * A `SettingsManager` is an object that can change settings of a given type.
 *
 * @param <S> the type of settings that are changed by the implementation
  */
interface SettingsManager<S : Settings<*>> {
    /**
     * Loads the default settings instance into the implementing object.
     */
    fun loadSettings()

    /**
     * Loads the given settings instance into the implementing object.
     *
     * @param settings a settings instance
     */
    fun loadSettings(settings: S)

    /**
     * Saves the implementing object's state into the default settings instance.
     */
    fun saveSettings()

    /**
     * Saves the implementing object's state into the given settings instance.
     *
     * @param settings a settings instance
     */
    fun saveSettings(settings: S)
}
