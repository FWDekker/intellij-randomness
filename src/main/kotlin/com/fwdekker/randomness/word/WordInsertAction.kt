package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.ui.JBPopupHelper
import java.util.concurrent.ThreadLocalRandom


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
    public override fun generateString(): String {
        // TODO Kotlin-ify
        val validationInfo = settings.validateActiveDictionaries()
        if (validationInfo != null) {
            JBPopupHelper.showMessagePopup(
                "Randomness error",
                validationInfo.message,
                "Please check your Randomness `word` settings."
            )
            return ""
        }

        val words = Dictionary.combine(settings.validActiveDictionaries)
            .getWordsWithLengthInRange(settings.minLength, settings.maxLength)
        if (words.isEmpty()) {
            JBPopupHelper.showMessagePopup(
                "Randomness error",
                "There are no words compatible with the current settings.",
                "Please check your Randomness `word` settings."
            )
            return ""
        }

        val randomIndex = ThreadLocalRandom.current().nextInt(0, words.size)
        val randomWord = settings.capitalization.transform(words[randomIndex])

        return settings.enclosure + randomWord + settings.enclosure
    }


    /**
     * Inserts an array of words.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert Word Array"
    }
}
