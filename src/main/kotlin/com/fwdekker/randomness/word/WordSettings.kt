package com.fwdekker.randomness.word

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.SettingsConfigurable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient


/**
 * The user-configurable collection of schemes applicable to generating words.
 *
 * @property schemes the schemes that the user can choose from
 * @property currentSchemeName the scheme that is currently active
 */
@State(name = "WordSettings", storages = [Storage("\$APP_CONFIG\$/randomness.xml")])
data class WordSettings(
    @MapAnnotation(sortBeforeSave = false)
    override var schemes: MutableList<WordScheme> = DEFAULT_SCHEMES.toMutableList(),
    override var currentSchemeName: String = Scheme.DEFAULT_NAME
) : Settings<WordSettings, WordScheme> {
    companion object {
        /**
         * The default value of the [schemes][schemes] field.
         */
        val DEFAULT_SCHEMES
            get() = listOf(WordScheme())

        /**
         * The persistent `WordSettings` instance.
         */
        val default: WordSettings
            get() = ServiceManager.getService(WordSettings::class.java)
    }


    override fun deepCopy() = copy(schemes = schemes.map { it.copy() }.toMutableList())

    override fun getState() = this

    override fun loadState(state: WordSettings) = XmlSerializerUtil.copyBean(state, this)
}


/**
 * Contains settings for generating random words.
 *
 * @property myName The name of the scheme.
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
data class WordScheme(
    override var myName: String = Scheme.DEFAULT_NAME,
    var minLength: Int = DEFAULT_MIN_LENGTH,
    var maxLength: Int = DEFAULT_MAX_LENGTH,
    var enclosure: String = DEFAULT_ENCLOSURE,
    var capitalization: CapitalizationMode = DEFAULT_CAPITALIZATION,
    @MapAnnotation(sortBeforeSave = false)
    var bundledDictionaryFiles: MutableSet<String> = DEFAULT_BUNDLED_DICTIONARY_FILES.toMutableSet(),
    @MapAnnotation(sortBeforeSave = false)
    var activeBundledDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES.toMutableSet(),
    @MapAnnotation(sortBeforeSave = false)
    var userDictionaryFiles: MutableSet<String> = DEFAULT_USER_DICTIONARY_FILES.toMutableSet(),
    @MapAnnotation(sortBeforeSave = false)
    var activeUserDictionaryFiles: MutableSet<String> = DEFAULT_ACTIVE_USER_DICTIONARY_FILES.toMutableSet()
) : Scheme<WordScheme> {
    companion object {
        /**
         * The default value of the [minLength][minLength] field.
         */
        const val DEFAULT_MIN_LENGTH = 3
        /**
         * The default value of the [maxLength][maxLength] field.
         */
        const val DEFAULT_MAX_LENGTH = 8
        /**
         * The default value of the [enclosure][enclosure] field.
         */
        const val DEFAULT_ENCLOSURE = "\""
        /**
         * The default value of the [capitalization][capitalization] field.
         */
        val DEFAULT_CAPITALIZATION = CapitalizationMode.RETAIN
        /**
         * The default value of the [bundledDictionaryFiles][bundledDictionaryFiles] field.
         */
        val DEFAULT_BUNDLED_DICTIONARY_FILES =
            setOf(BundledDictionary.SIMPLE_DICTIONARY, BundledDictionary.EXTENDED_DICTIONARY)
        /**
         * The default value of the [activeBundledDictionaryFiles][activeBundledDictionaryFiles] field.
         */
        val DEFAULT_ACTIVE_BUNDLED_DICTIONARY_FILES = setOf(BundledDictionary.SIMPLE_DICTIONARY)
        /**
         * The default value of the [userDictionaryFiles][userDictionaryFiles] field.
         */
        val DEFAULT_USER_DICTIONARY_FILES = setOf<String>()
        /**
         * The default value of the [activeUserDictionaryFiles][activeUserDictionaryFiles] field.
         */
        val DEFAULT_ACTIVE_USER_DICTIONARY_FILES = setOf<String>()
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


    override fun copyFrom(other: WordScheme) = XmlSerializerUtil.copyBean(other, this)

    override fun copyAs(name: String) = this.copy(myName = name)
}


/**
 * The configurable for word settings.
 *
 * @see WordSettingsAction
 */
class WordSettingsConfigurable(
    override val component: WordSettingsComponent = WordSettingsComponent()
) : SettingsConfigurable<WordSettings, WordScheme>() {
    override fun getDisplayName() = "Words"
}
