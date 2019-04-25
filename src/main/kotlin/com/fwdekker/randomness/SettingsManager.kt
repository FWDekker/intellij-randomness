package com.fwdekker.randomness


/**
 * A `SettingsManager` is an object that can save and load settings.
 *
 * @param <S> the type of settings that is saved and loaded
 */
interface SettingsManager<S : Settings<*>> {
    /**
     * Loads the default settings object.
     */
    fun loadSettings()

    /**
     * Loads `settings`.
     *
     * @param settings the settings to load
     */
    fun loadSettings(settings: S)

    /**
     * Saves settings to the default settings object.
     */
    fun saveSettings()

    /**
     * Saves settings to `settings`.
     *
     * @param settings the settings to save to
     */
    fun saveSettings(settings: S)
}
