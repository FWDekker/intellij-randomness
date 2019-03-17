package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.ui.JBPopupHelper


/**
 * Generates random alphanumerical English words based on the settings in [WordSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [WordSettings.default]
 */
class WordInsertAction(private val settings: WordSettings = WordSettings.default) : DataInsertAction() {
    // TODO Make array action name property as well by making superclass generate the subclass
    override val name = "Insert Word"


    /**
     * Returns a random alphanumerical English word.
     *
     * @return a random alphanumerical English word
     */
    override fun generateString(): String {
        // TODO Move error checking to caller. Return null in this method or throw exception?
        val words: Set<String>
        try {
            val bundledWords = settings.activeBundledDictionaries.flatMap { it.words }
            val userWords = settings.activeUserDictionaries.flatMap { it.words }

            words = (bundledWords + userWords)
                .filter { it.length in (settings.minLength..settings.maxLength) }
                .toSet()
        } catch (e: InvalidDictionaryException) {
            JBPopupHelper.showMessagePopup(
                "Randomness error",
                e.message ?: "An unknown error occurred while generating a random word.",
                "Please check your Randomness `word` settings and try again."
            )
            return ""
        }

        if (words.isEmpty()) {
            JBPopupHelper.showMessagePopup(
                "Randomness error",
                "There are no words compatible with the current settings.",
                "Please check your Randomness `word` settings and try again."
            )
            return ""
        }

        val randomWord = settings.capitalization.transform(words.random())
        return settings.enclosure + randomWord + settings.enclosure
    }


    /**
     * Inserts an array of words.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert Word Array"
    }
}
