package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.Arrays
import java.util.HashSet


/**
 * Contains settings for generating random words.
 */
@State(name = "WordSettings", storages = [Storage("\$APP_CONFIG$/randomness.xml")])
class WordSettings : Settings<WordSettings> {
    companion object {
        private const val DEFAULT_MIN_LENGTH = 3
        private const val DEFAULT_MAX_LENGTH = 8


        /**
         * The singleton `WordSettings` instance.
         */
        val instance: WordSettings
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
     * The list of all dictionaries provided by the plugin.
     */
    var bundledDictionaries: Set<String> = HashSet(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE))
    /**
     * The list of all dictionaries registered by the user.
     */
    var userDictionaries: Set<String> = HashSet()
    /**
     * The list of bundled dictionaries that are currently active.
     */
    var activeBundledDictionaries: Set<String> = HashSet(Arrays.asList(Dictionary.DEFAULT_DICTIONARY_FILE))
    /**
     * The list of user dictionaries that are currently active.
     */
    var activeUserDictionaries: Set<String> = HashSet()

    /**
     * The list of all dictionaries that are valid.
     */
    val validAllDictionaries: List<Dictionary>
        get() = getValidDictionaries(bundledDictionaries, userDictionaries)
    /**
     * The list of all dictionaries that are valid and currently active.
     */
    val validActiveDictionaries: List<Dictionary>
        get() = getValidDictionaries(activeBundledDictionaries, activeUserDictionaries)


    override fun getState() = this

    override fun loadState(state: WordSettings) = XmlSerializerUtil.copyBean(state, this)


    /**
     * Validates all dictionaries.
     *
     * @return `null` if all dictionaries are valid, or a `ValidationInfo` explaining why there is an
     * invalid dictionary
     */
    fun validateAllDictionaries() = validateDictionaries(bundledDictionaries, userDictionaries)

    /**
     * Validates all dictionaries that are currently active.
     *
     * @return `null` if all dictionaries are valid, or a `ValidationInfo` explaining why there is an
     * invalid dictionary
     */
    fun validateActiveDictionaries() = validateDictionaries(activeBundledDictionaries, activeUserDictionaries)


    /**
     * Returns the list of all dictionaries in the given collections that are valid.
     *
     * @param bundledDictionaries a list of `BundledDictionary`s
     * @param userDictionaries    a list of `UserDictionary`s
     * @return the list of all dictionaries in the given lists that are valid
     */
    private fun getValidDictionaries(bundledDictionaries: Collection<String>, userDictionaries: Collection<String>) =
        mutableListOf<Dictionary>() +
            bundledDictionaries
                .filter { dictionary -> Dictionary.BundledDictionary.validate(dictionary) == null }
                .map { dictionary -> Dictionary.BundledDictionary.get(dictionary) } +
            userDictionaries
                .filter { dictionary -> Dictionary.UserDictionary.validate(dictionary) == null }
                .map { Dictionary.UserDictionary.get(it) }

    /**
     * Validates all dictionaries in the given collections.
     *
     * @param bundledDictionaries a list of `BundledDictionary`s
     * @param userDictionaries    a list of `UserDictionary`s
     * @return `null` if all dictionaries are valid, or a `ValidationInfo` explaining why there is an
     * invalid dictionary
     */
    private fun validateDictionaries(bundledDictionaries: Collection<String>, userDictionaries: Collection<String>) =
        null
            ?: bundledDictionaries
                .mapNotNull { Dictionary.BundledDictionary.validate(it) }
                .firstOrNull()
            ?: userDictionaries
                .mapNotNull { Dictionary.UserDictionary.validate(it) }
                .firstOrNull()
}
