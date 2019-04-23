package com.fwdekker.randomness.word

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
 * Generates random alphanumerical English words based on the settings in [WordSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [WordSettings.default]
 */
class WordInsertAction(private val settings: WordSettings = WordSettings.default) : DataInsertAction() {
    override val name = "Insert Word"


    /**
     * Returns a random alphanumerical English word.
     *
     * @return a random alphanumerical English word
     */
    override fun generateString(): String {
        val bundledWords: List<String>
        val userWords: List<String>
        try {
            bundledWords = settings.activeBundledDictionaries.flatMap { it.words }
            userWords = settings.activeUserDictionaries.flatMap { it.words }
        } catch (e: InvalidDictionaryException) {
            throw StringGenerationException(e.message)
        }

        val words = (bundledWords + userWords)
            .filter { it.length in (settings.minLength..settings.maxLength) }
            .toSet()
        if (words.isEmpty())
            throw StringGenerationException("There are no words compatible with the current settings.")

        val randomWord = settings.capitalization.transform(words.random())
        return settings.enclosure + randomWord + settings.enclosure
    }
}


/**
 * Inserts an array of words.
 */
class WordInsertArrayAction(settings: WordSettings = WordSettings.default) :
    DataInsertArrayAction(WordInsertAction(settings)) {
    override val name = "Insert Word Array"
}


/**
 * Controller for random string generation settings.
 */
class WordSettingsAction : SettingsAction() {
    override val title = "Word Settings"


    public override fun createDialog() = WordSettingsDialog()
}
