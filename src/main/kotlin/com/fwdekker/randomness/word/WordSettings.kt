package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient


/**
 * Contains settings for generating random words.
 *
 * @property minLength The minimum length of the generated word, inclusive.
 * @property maxLength The maximum length of the generated word, inclusive.
 * @property enclosure The string that encloses the generated word on both sides.
 * @property capitalization The way in which the generated word should be capitalized.
 * @property bundledDictionaryFiles The list of all dictionary files provided by the plugin.
 * @property userDictionaryFiles The list of all dictionary files registered by the user.
 * @property activeBundledDictionaryFiles The list of bundled dictionary files that are currently active; a subset of
 * [bundledDictionaryFiles].
 * @property activeUserDictionaryFiles The list of user dictionary files that are currently active; a subset of
 * [userDictionaryFiles].
 *
 * @see WordInsertAction
 * @see WordSettingsAction
 * @see WordSettingsComponent
 */
@State(name = "WordSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class WordSettings(
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    var bundledDictionaryFiles: MutableSet<String> =
        mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY, BundledDictionary.EXTENDED_DICTIONARY),
    var userDictionaryFiles: MutableSet<String> =
        mutableSetOf(),
    var activeBundledDictionaryFiles: MutableSet<String> =
        mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY),
    var activeUserDictionaryFiles: MutableSet<String> =
        mutableSetOf()
) : Settings<WordSettings> {
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


    override fun copyState() = WordSettings().also { it.loadState(this) }

    override fun getState() = this

    override fun loadState(state: WordSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * The configurable for word settings.
 *
 * @see WordSettingsAction
 */
class WordSettingsConfigurable(
    override val component: WordSettingsComponent = WordSettingsComponent()
) : SettingsConfigurable<WordSettings>() {
    override fun getDisplayName() = "Words"
}
