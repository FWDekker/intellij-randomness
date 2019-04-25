package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction


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
     * Returns a random word from the dictionaries in `settings`.
     *
     * @return a random word from the dictionaries in `settings`
     * @throws InvalidDictionaryException if no words could be found using the settings in `settings`
     */
    override fun generateString(): String {
        val bundledWords: List<String>
        val userWords: List<String>
        try {
            bundledWords = settings.activeBundledDictionaries.flatMap { it.words }
            userWords = settings.activeUserDictionaries.flatMap { it.words }
        } catch (e: InvalidDictionaryException) {
            throw DataGenerationException(e.message, e)
        }

        val words = (bundledWords + userWords)
            .filter { it.length in settings.minLength..settings.maxLength }
            .toSet()
        if (words.isEmpty())
            throw DataGenerationException("There are no words compatible with the current settings.")

        val randomWord = settings.capitalization.transform(words.random())
        return settings.enclosure + randomWord + settings.enclosure
    }
}


/**
 * Inserts an array-like string of words.
 *
 * @param settings the settings to use for generating words
 *
 * @see WordInsertAction
 */
class WordInsertArrayAction(settings: WordSettings = WordSettings.default) :
    DataInsertArrayAction(WordInsertAction(settings)) {
    override val name = "Insert Word Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see WordSettings
 * @see WordSettingsDialog
 */
class WordSettingsAction : SettingsAction() {
    override val title = "Word Settings"


    public override fun createDialog() = WordSettingsDialog()
}
