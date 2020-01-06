package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import icons.RandomnessIcons


/**
 * All actions related to inserting words.
 */
class WordGroupAction : DataGroupAction(RandomnessIcons.Word.Base) {
    override val insertAction = WordInsertAction()
    override val insertArrayAction = WordInsertArrayAction()
    override val insertRepeatAction = WordInsertRepeatAction()
    override val insertRepeatArrayAction = WordInsertRepeatArrayAction()
    override val settingsAction = WordSettingsAction()
}


/**
 * Inserts random words.
 *
 * @param scheme the scheme to use for generating words
 *
 * @see WordInsertArrayAction
 * @see WordSettings
 */
class WordInsertAction(private val scheme: WordScheme = WordSettings.default.currentScheme) :
    DataInsertAction(RandomnessIcons.Word.Base) {
    override val name = "Random Word"


    /**
     * Returns random words from the dictionaries in `settings`.
     *
     * @param count the number of words to generate
     * @return random words from the dictionaries in `settings`
     * @throws InvalidDictionaryException if no words could be found using the settings in `settings`
     */
    override fun generateStrings(count: Int): List<String> {
        val dictionaries = (scheme.activeBundledDictionaries + scheme.activeUserDictionaries)
            .ifEmpty { throw DataGenerationException("There are no active dictionaries.") }

        val words =
            try {
                dictionaries.flatMap { it.words }
            } catch (e: InvalidDictionaryException) {
                throw DataGenerationException(e.message, e)
            }
                .ifEmpty { throw DataGenerationException("All active dictionaries are empty.") }
                .filter { it.length in scheme.minLength..scheme.maxLength }
                .toSet()
                .ifEmpty { throw DataGenerationException("There are no words within the configured length range.") }

        return (0 until count)
            .map { words.random(random) }
            .map { scheme.capitalization.transform(it) }
            .map { scheme.enclosure + it + scheme.enclosure }
    }
}


/**
 * Inserts an array-like string of words.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param scheme the scheme to use for generating words
 *
 * @see WordInsertAction
 */
class WordInsertArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    scheme: WordScheme = WordSettings.default.currentScheme
) : DataInsertArrayAction(arrayScheme, WordInsertAction(scheme), RandomnessIcons.Word.Array) {
    override val name = "Random Word Array"
}


/**
 * Inserts repeated random words.
 *
 * @param scheme the settings to use for generating words
 */
class WordInsertRepeatAction(scheme: WordScheme = WordSettings.default.currentScheme) :
    DataInsertRepeatAction(WordInsertAction(scheme), RandomnessIcons.Word.Repeat) {
    override val name = "Random Repeated Word"
}


/**
 * Inserts repeated array-like strings of words.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param scheme the scheme to use for generating words
 */
class WordInsertRepeatArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    scheme: WordScheme = WordSettings.default.currentScheme
) : DataInsertRepeatArrayAction(WordInsertArrayAction(arrayScheme, scheme), RandomnessIcons.Word.RepeatArray) {
    override val name = "Random Repeated Word Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see WordSettings
 * @see WordSettingsComponent
 */
class WordSettingsAction : DataSettingsAction(RandomnessIcons.Word.Settings) {
    override val title = "Word Settings"

    override val configurableClass = WordSettingsConfigurable::class.java
}
