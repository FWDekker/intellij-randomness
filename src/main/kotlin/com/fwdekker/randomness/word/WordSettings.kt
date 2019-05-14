package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient


/**
 * Contains settings for generating random words.
 *
 * @see WordInsertAction
 * @see WordSettingsAction
 * @see WordSettingsDialog
 */
@State(name = "WordSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class WordSettings : Settings<WordSettings> {
    companion object {
        /**
         * The default value of the [minLength][WordSettings.minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3
        /**
         * The default value of the [maxLength][WordSettings.maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8
        /**
         * The default value of the [enclosure][WordSettings.enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""
        /**
         * The default value of the [capitalization][WordSettings.capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN


        /**
         * The persistent `WordSettings` instance.
         */
        val default: WordSettings
            get() = ServiceManager.getService(WordSettings::class.java)
    }


    /**
     * The minimum length of the generated word, inclusive.
     */
    var minLength = DEFAULT_MIN_LENGTH
    /**
     * The maximum length of the generated word, inclusive.
     */
    var maxLength = DEFAULT_MAX_LENGTH
    /**
     * The string that encloses the generated word on both sides.
     */
    var enclosure = DEFAULT_ENCLOSURE
    /**
     * The way in which the generated word should be capitalized.
     */
    var capitalization = DEFAULT_CAPITALIZATION
    /**
     * The list of all dictionary files provided by the plugin.
     */
    var bundledDictionaryFiles =
        mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY, BundledDictionary.EXTENDED_DICTIONARY)
    /**
     * The list of all dictionary files registered by the user.
     */
    var userDictionaryFiles: MutableSet<String> = mutableSetOf()
    /**
     * The list of bundled dictionary files that are currently active.
     *
     * This is a subset of [bundledDictionaryFiles].
     */
    var activeBundledDictionaryFiles = mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY)
    /**
     * The list of user dictionary files that are currently active.
     *
     * This is a subset of [userDictionaries].
     */
    var activeUserDictionaryFiles: MutableSet<String> = mutableSetOf()
    /**
     * A mutable view of the filenames of the files in [bundledDictionaryFiles].
     */
    var bundledDictionaries: Set<BundledDictionary>
        @Transient
        get() = bundledDictionaryFiles.map { BundledDictionary.cache.get(it) }.toSet()
        set(value) {
            bundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    /**
     * A mutable view of the filenames of the files in [userDictionaryFiles].
     */
    var userDictionaries: Set<UserDictionary>
        @Transient
        get() = userDictionaryFiles.map { UserDictionary.cache.get(it) }.toSet()
        set(value) {
            userDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    /**
     * A mutable view of the filenames of the files in [activeBundledDictionaryFiles].
     */
    var activeBundledDictionaries: Set<BundledDictionary>
        @Transient
        get() = activeBundledDictionaryFiles.map { BundledDictionary.cache.get(it) }.toSet()
        set(value) {
            activeBundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    /**
     * A mutable view of the filenames of the files in [activeUserDictionaryFiles].
     */
    var activeUserDictionaries: Set<UserDictionary>
        @Transient
        get() = activeUserDictionaryFiles.map { UserDictionary.cache.get(it) }.toSet()
        set(value) {
            activeUserDictionaryFiles = value.map { it.filename }.toMutableSet()
        }


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
    override fun loadState(state: WordSettings) = XmlSerializerUtil.copyBean(state, this)
}
