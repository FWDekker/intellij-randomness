package com.fwdekker.randomness.word

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.Transient


/**
 * The user-configurable persistent collection of all [Dictionaries][Dictionary] available to the user.
 *
 * @property bundledDictionaryFiles The list of all dictionary files provided by the plugin.
 * @property userDictionaryFiles The list of all dictionary files registered by the user.
 */
@State(
    name = "com.fwdekker.randomness.word.DictionarySettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
data class DictionarySettings(
    @MapAnnotation(sortBeforeSave = false)
    var bundledDictionaryFiles: MutableSet<String> = DEFAULT_BUNDLED_DICTIONARY_FILES,
    @MapAnnotation(sortBeforeSave = false)
    var userDictionaryFiles: MutableSet<String> = DEFAULT_USER_DICTIONARY_FILES
) : Settings<DictionarySettings>() {
    /**
     * A mutable view of the filenames of the files in [bundledDictionaryFiles].
     */
    var bundledDictionaries: Set<DictionaryReference>
        @Transient
        get() = bundledDictionaryFiles.map { DictionaryReference(true, it) }.toSet()
        set(value) {
            bundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }

    /**
     * A mutable view of the filenames of the files in [userDictionaryFiles].
     */
    var userDictionaries: Set<DictionaryReference>
        @Transient
        get() = userDictionaryFiles.map { DictionaryReference(false, it) }.toSet()
        set(value) {
            userDictionaryFiles = value.map { it.filename }.toMutableSet()
        }


    override fun getState() = this

    override fun deepCopy() =
        copy().also {
            it.bundledDictionaryFiles = bundledDictionaryFiles.toMutableSet()
            it.userDictionaryFiles = userDictionaryFiles.toMutableSet()
        }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [bundledDictionaryFiles][bundledDictionaryFiles] field.
         */
        val DEFAULT_BUNDLED_DICTIONARY_FILES: MutableSet<String>
            get() = mutableSetOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [userDictionaryFiles][userDictionaryFiles] field.
         */
        val DEFAULT_USER_DICTIONARY_FILES: MutableSet<String>
            get() = mutableSetOf()

        /**
         * The persistent `DictionarySettings` instance.
         */
        val default: DictionarySettings
            get() = service()
    }
}
