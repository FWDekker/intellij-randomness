package com.fwdekker.randomness.word

import com.fwdekker.randomness.Settings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.XCollection


/**
 * The user-configurable persistent collection of all [Dictionaries][Dictionary] available to the user.
 *
 * @property dictionaries The list of all dictionaries configured by the user.
 */
@State(
    name = "com.fwdekker.randomness.word.DictionarySettings",
    storages = [Storage("\$APP_CONFIG\$/randomness.xml")]
)
data class DictionarySettings(
    @get:XCollection(elementTypes = [BundledDictionary::class, UserDictionary::class])
    var dictionaries: MutableSet<Dictionary> = DEFAULT_DICTIONARIES.toMutableSet(),
) : PersistentStateComponent<DictionarySettings>, Settings() {
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
        UserDictionary.clearCache()

        return dictionaries.firstNotNullOfOrNull {
            try {
                if (it.words.isEmpty()) return@firstNotNullOfOrNull "Dictionary '$it' is empty."
                else null
            } catch (e: InvalidDictionaryException) {
                return@firstNotNullOfOrNull "Dictionary '$it' is invalid: ${e.message}"
            }
        }
    }

    override fun deepCopy(retainUuid: Boolean) =
        copy(dictionaries = dictionaries.map { it.deepCopy() }.toMutableSet())
            .also { if (retainUuid) it.uuid = uuid }


    /**
     * Holds constants.
     */
    companion object {
        /**
         * The default value of the [dictionaries] field.
         */
        val DEFAULT_DICTIONARIES: Set<Dictionary>
            get() = setOf(BundledDictionary(BundledDictionary.SIMPLE_DICTIONARY))

        /**
         * The persistent `DictionarySettings` instance.
         */
        val default: DictionarySettings
            get() = service()
    }
}
