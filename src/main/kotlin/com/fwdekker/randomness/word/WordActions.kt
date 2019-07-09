package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings


/**
 * All actions related to inserting words.
 */
class WordGroupAction : DataGroupAction() {
    override val insertAction = WordInsertAction()
    override val insertArrayAction = WordInsertArrayAction()
    override val settingsAction = WordSettingsAction()
}


/**
 * Inserts random words.
 *
 * @param settings the settings to use for generating words
 *
 * @see WordInsertArrayAction
 * @see WordSettings
 */
class WordInsertAction(private val settings: WordSettings = WordSettings.default) : DataInsertAction() {
    override val name = "Insert Word"


    /**
     * Returns random words from the dictionaries in `settings`.
     *
     * @param count the number of words to generate
     * @return random words from the dictionaries in `settings`
     * @throws InvalidDictionaryException if no words could be found using the settings in `settings`
     */
    override fun generateStrings(count: Int): List<String> {
        val dictionaries = (settings.activeBundledDictionaries + settings.activeUserDictionaries)
            .ifEmpty { throw DataGenerationException("There are no active dictionaries.") }

        val words =
            try {
                dictionaries.flatMap { it.words }
            } catch (e: InvalidDictionaryException) {
                throw DataGenerationException(e.message, e)
            }
                .ifEmpty { throw DataGenerationException("All active dictionaries are empty.") }
                .filter { it.length in settings.minLength..settings.maxLength }
                .toSet()
                .ifEmpty { throw DataGenerationException("There are no words within the configured length range.") }

        return (0 until count)
            .map { words.random() }
            .map { settings.capitalization.transform(it) }
            .map { settings.enclosure + it + settings.enclosure }
    }
}


/**
 * Inserts an array-like string of words.
 *
 * @param arraySettings the settings to use for generating arrays
 * @param settings the settings to use for generating words
 *
 * @see WordInsertAction
 */
class WordInsertArrayAction(
    arraySettings: ArraySettings = ArraySettings.default,
    settings: WordSettings = WordSettings.default
) : DataInsertArrayAction(arraySettings, WordInsertAction(settings)) {
    override val name = "Insert Word Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see WordSettings
 * @see WordSettingsDialog
 */
class WordSettingsAction : SettingsAction<WordSettings>() {
    override val title = "Word Settings"

    override val configurableClass = WordSettingsConfigurable::class.java
}
