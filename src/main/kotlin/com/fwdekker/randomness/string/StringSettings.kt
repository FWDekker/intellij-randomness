package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


/**
 * Contains settings for generating random strings.
 *
 * @see StringInsertAction
 * @see StringSettingsAction
 * @see StringSettingsDialog
 */
@State(name = "StringSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class StringSettings : Settings<StringSettings> {
    companion object {
        /**
         * The default value of the [minLength][StringSettings.minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3
        /**
         * The default value of the [maxLength][StringSettings.maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8
        /**
         * The default value of the [enclosure][StringSettings.enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""
        /**
         * The default value of the [capitalization][StringSettings.capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RANDOM


        /**
         * The persistent `StringSettings` instance.
         */
        val default: StringSettings
            get() = ServiceManager.getService(StringSettings::class.java)
    }


    /**
     * The minimum length of the generated string, inclusive.
     */
    var minLength = DEFAULT_MIN_LENGTH
    /**
     * The maximum length of the generated string, inclusive.
     */
    var maxLength = DEFAULT_MAX_LENGTH
    /**
     * The string that encloses the generated string on both sides.
     */
    var enclosure = DEFAULT_ENCLOSURE
    /**
     * The capitalization mode of the generated string.
     */
    var capitalization = DEFAULT_CAPITALIZATION
    /**
     * The alphabets to be used for generating strings.
     */
    var alphabets = mutableSetOf(Alphabet.ALPHABET, Alphabet.DIGITS)


    /**
     * Returns `this`.
     *
     * @return `this`
     */
    override fun getState() = this

    /**
     * Copies the fields of [state] to `this`.
     *
     * @param state the state to load into `this`
     */
    override fun loadState(state: StringSettings) = XmlSerializerUtil.copyBean(state, this)
}
