package com.fwdekker.randomness.string

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
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction(RandomnessIcons.String.Base) {
    override val insertAction = StringInsertAction()
    override val insertArrayAction = StringInsertArrayAction()
    override val insertRepeatAction = StringInsertRepeatAction()
    override val insertRepeatArrayAction = StringInsertRepeatArrayAction()
    override val settingsAction = StringSettingsAction()
}


/**
 * Inserts random alphanumerical strings.
 *
 * @param scheme the scheme to use for generating strings
 *
 * @see StringInsertArrayAction
 * @see StringSettings
 */
class StringInsertAction(private val scheme: StringScheme = StringSettings.default.currentScheme) :
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
            if (scheme.minLength > scheme.maxLength)
                throw DataGenerationException("Minimum length is larger than maximum length.")

            val length = random.nextInt(scheme.minLength, scheme.maxLength + 1)

            val text = List(length) { generateCharacter() }.joinToString("")
            val capitalizedText = scheme.capitalization.transform(text)

            scheme.enclosure + capitalizedText + scheme.enclosure
        }


    /**
     * Returns a random character from the symbol sets in `settings`.
     *
     * @return a random character from the symbol sets in `settings`
     * @throws DataGenerationException if a random character could not be generated
     */
    @Throws(DataGenerationException::class)
    private fun generateCharacter(): Char {
        val symbolSet = scheme.activeSymbolSetList.sum()
        if (symbolSet.isEmpty())
            throw DataGenerationException("No active symbol sets.")

        val charIndex = random.nextInt(symbolSet.length)

        return symbolSet[charIndex]
    }
}


/**
 * Inserts an array-like string of strings.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param scheme the scheme to use for generating strings
 *
 * @see StringInsertAction
 */
class StringInsertArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    scheme: StringScheme = StringSettings.default.currentScheme
) : DataInsertArrayAction(arrayScheme, StringInsertAction(scheme), RandomnessIcons.String.Array) {
    override val name = "Random String Array"
}


/**
 * Inserts repeated random strings.
 *
 * @param scheme the settings to use for generating strings
 */
class StringInsertRepeatAction(scheme: StringScheme = StringSettings.default.currentScheme) :
    DataInsertRepeatAction(StringInsertAction(scheme), RandomnessIcons.String.Repeat) {
    override val name = "Random Repeated String"
}


/**
 * Inserts repeated array-like strings of strings.
 *
 * @param arrayScheme the scheme to use for generating arrays
 * @param scheme the scheme to use for generating strings
 */
class StringInsertRepeatArrayAction(
    arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
    scheme: StringScheme = StringSettings.default.currentScheme
) : DataInsertRepeatArrayAction(StringInsertArrayAction(arrayScheme, scheme), RandomnessIcons.String.RepeatArray) {
    override val name = "Random Repeated String Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see StringSettings
 * @see StringSettingsComponent
 */
class StringSettingsAction : DataSettingsAction(RandomnessIcons.String.Settings) {
    override val title = "String Settings"

    override val configurableClass = StringSettingsConfigurable::class.java
}
