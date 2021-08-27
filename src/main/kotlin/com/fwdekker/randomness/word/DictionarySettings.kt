package com.fwdekker.randomness.word

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.PersistentStateComponent
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
    var bundledDictionaryFiles: MutableSet<String> = DEFAULT_BUNDLED_DICTIONARY_FILES.toMutableSet(),
    @MapAnnotation(sortBeforeSave = false)
    var userDictionaryFiles: MutableSet<String> = DEFAULT_USER_DICTIONARY_FILES.toMutableSet()
) : PersistentStateComponent<DictionarySettings>, Settings() {
    /**
     * A view of the filenames of the files in [bundledDictionaryFiles].
     */
    var bundledDictionaries: Set<DictionaryReference>
        @Transient
        get() = bundledDictionaryFiles.map { DictionaryReference(true, it) }.toSet()
        set(value) {
            bundledDictionaryFiles = value.map { it.filename }.toMutableSet()
        }

    /**
     * A view of the filenames of the files in [userDictionaryFiles].
     */
    var userDictionaries: Set<DictionaryReference>
        @Transient
        get() = userDictionaryFiles.map { DictionaryReference(false, it) }.toSet()
        set(value) {
            userDictionaryFiles = value.map { it.filename }.toMutableSet()
        }


    /**
     * Returns this instance.
     *
     * @return this instance
     */
    override fun getState() = this

    /**
     * Invokes [copyFrom].
     *
     * @param state the state to invoke [copyFrom] on
     */
    override fun loadState(state: DictionarySettings) = copyFrom(state)


    override fun doValidate(): String? {
        BundledDictionary.cache.clear()
        UserDictionary.cache.clear()

        return (bundledDictionaries + userDictionaries)
            .associateWith { dictionary ->
                try {
                    dictionary.words
                } catch (e: InvalidDictionaryException) {
                    return "Dictionary '$dictionary' is invalid: ${e.message}"
                }
            }
            .toList()
            .firstOrNull { it.second.isEmpty() }
            ?.let { "Dictionary '${it.first.filename}' is empty." }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(
            bundledDictionaryFiles = bundledDictionaryFiles.toMutableSet(),
            userDictionaryFiles = userDictionaryFiles.toMutableSet(),
        ).also { if (retainUuid) it.uuid = uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [bundledDictionaryFiles][bundledDictionaryFiles] field.
         */
        val DEFAULT_BUNDLED_DICTIONARY_FILES: Set<String>
            get() = setOf(BundledDictionary.SIMPLE_DICTIONARY)

        /**
         * The default value of the [userDictionaryFiles][userDictionaryFiles] field.
         */
        val DEFAULT_USER_DICTIONARY_FILES: Set<String>
            get() = setOf()

        /**
         * The persistent `DictionarySettings` instance.
         */
        val default: DictionarySettings
            get() = service()
    }
}
