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
 */
@State(name = "WordSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
class WordSettings : Settings<WordSettings> {
    companion object {
        private const val DEFAULT_MIN_LENGTH = 3
        private const val DEFAULT_MAX_LENGTH = 8


        /**
         * The singleton `WordSettings` instance.
         */
        val default: WordSettings
            get() = ServiceManager.getService(WordSettings::class.java)
    }


    /**
     * The minimum length of a generated word, inclusive.
     */
    var minLength = DEFAULT_MIN_LENGTH
    /**
     * The maximum length of a generated word, inclusive.
     */
    var maxLength = DEFAULT_MAX_LENGTH
    /**
     * The string that encloses the generated word on both sides.
     */
    var enclosure = "\""
    /**
     * The way in which the generated word should be capitalized.
     */
    var capitalization = CapitalizationMode.RETAIN
    /**
     * The list of all dictionary files provided by the plugin.
     */
    var bundledDictionaryFiles: MutableSet<String> = mutableSetOf(BundledDictionary.DEFAULT_DICTIONARY_FILE)
    /**
     * The list of all dictionary files registered by the user.
     */
    var userDictionaryFiles: MutableSet<String> = mutableSetOf()
    /**
     * The list of bundled dictionary files that are currently active.
     *
     * This is a subset of [bundledDictionaryFiles].
     */
    var activeBundledDictionaryFiles: MutableSet<String> = mutableSetOf(BundledDictionary.DEFAULT_DICTIONARY_FILE)
    /**
     * The list of user dictionary files that are currently active.
     *
     * This is a subset of [userDictionaries].
     */
    var activeUserDictionaryFiles: MutableSet<String> = mutableSetOf()
    var bundledDictionaries: Set<BundledDictionary>
        @Transient
        get() = bundledDictionaryFiles.map { BundledDictionary.cache.get(it) }.toSet()
        set(value) {
            bundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    var userDictionaries: Set<UserDictionary>
        @Transient
        get() = userDictionaryFiles.map { UserDictionary.cache.get(it) }.toSet()
        set(value) {
            userDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    var activeBundledDictionaries: Set<BundledDictionary>
        @Transient
        get() = activeBundledDictionaryFiles.map { BundledDictionary.cache.get(it) }.toSet()
        set(value) {
            activeBundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }
    var activeUserDictionaries: Set<UserDictionary>
        @Transient
        get() = activeUserDictionaryFiles.map { UserDictionary.cache.get(it) }.toSet()
        set(value) {
            activeUserDictionaryFiles = value.map { it.filename }.toMutableSet()
        }


    override fun getState() = this

    override fun loadState(state: WordSettings) = XmlSerializerUtil.copyBean(state, this)
}
