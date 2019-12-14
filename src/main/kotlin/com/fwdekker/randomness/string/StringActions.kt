package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import com.fwdekker.randomness.array.ArraySettings
import icons.RandomnessIcons


/**
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction(RandomnessIcons.String.Base) {
    override val insertAction = StringInsertAction()
    override val insertArrayAction = StringInsertArrayAction()
    override val settingsAction = StringSettingsAction()
}


/**
 * Inserts random alphanumerical strings.
 *
 * @param settings the settings to use for generating strings
 *
 * @see StringInsertArrayAction
 * @see StringSettings
 */
class StringInsertAction(private val settings: StringSettings = StringSettings.default) :
    DataInsertAction(RandomnessIcons.String.Base) {
    override val name = "Random String"


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateStrings(count: Int) =
        List(count) {
            if (settings.minLength > settings.maxLength)
                throw DataGenerationException("Minimum length is larger than maximum length.")

            val length = random.nextInt(settings.minLength, settings.maxLength + 1)

            val text = List(length) { generateCharacter() }.joinToString("")
            val capitalizedText = settings.capitalization.transform(text)

            settings.enclosure + capitalizedText + settings.enclosure
        }


    /**
     * Returns a random character from the symbol sets in `settings`.
     *
     * @return a random character from the symbol sets in `settings`
     * @throws DataGenerationException if a random character could not be generated
     */
    @Throws(DataGenerationException::class)
    private fun generateCharacter(): Char {
        val symbolSet = settings.activeSymbolSetList.sum()
        if (symbolSet.isEmpty())
            throw DataGenerationException("No active symbol sets.")

        val charIndex = random.nextInt(symbolSet.length)

        return symbolSet[charIndex]
    }
}


/**
 * Inserts an array-like string of strings.
 *
 * @param arraySettings the settings to use for generating arrays
 * @param settings the settings to use for generating strings
 *
 * @see StringInsertAction
 */
class StringInsertArrayAction(
    arraySettings: ArraySettings = ArraySettings.default,
    settings: StringSettings = StringSettings.default
) : DataInsertArrayAction(arraySettings, StringInsertAction(settings), RandomnessIcons.String.Array) {
    override val name = "Random String Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see StringSettings
 * @see StringSettingsComponent
 */
class StringSettingsAction : SettingsAction<StringSettings>(RandomnessIcons.String.Settings) {
    override val title = "String Settings"

    override val configurableClass = StringSettingsConfigurable::class.java
}
