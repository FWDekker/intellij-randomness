package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.DataInsertRepeatAction
import com.fwdekker.randomness.DataInsertRepeatArrayAction
import com.fwdekker.randomness.DataQuickSwitchSchemeAction
import com.fwdekker.randomness.DataSettingsAction
import com.fwdekker.randomness.array.ArrayScheme
import com.fwdekker.randomness.array.ArraySettings
import com.fwdekker.randomness.array.ArraySettingsAction
import icons.RandomnessIcons


/**
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction(RandomnessIcons.String.Base) {
    override val insertAction = StringInsertAction()
    override val insertArrayAction = StringInsertAction.ArrayAction()
    override val insertRepeatAction = StringInsertAction.RepeatAction()
    override val insertRepeatArrayAction = StringInsertAction.RepeatArrayAction()
    override val settingsAction = StringSettingsAction()
    override val quickSwitchSchemeAction = StringSettingsAction.StringQuickSwitchSchemeAction()
    override val quickSwitchArraySchemeAction = ArraySettingsAction.ArrayQuickSwitchSchemeAction()
}


/**
 * Inserts random alphanumerical strings.
 *
 * @param scheme the scheme to use for generating strings
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
        val symbolSet = scheme.activeSymbolSetList.sum(scheme.excludeLookAlikeSymbols)
        if (symbolSet.isEmpty())
            throw DataGenerationException("No valid symbols found in active symbol sets.")

        val charIndex = random.nextInt(symbolSet.length)

        return symbolSet[charIndex]
    }


    /**
     * Inserts an array-like string of strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class ArrayAction(
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
    class RepeatAction(scheme: StringScheme = StringSettings.default.currentScheme) :
        DataInsertRepeatAction(StringInsertAction(scheme), RandomnessIcons.String.Repeat) {
        override val name = "Random Repeated String"
    }

    /**
     * Inserts repeated array-like strings of strings.
     *
     * @param arrayScheme the scheme to use for generating arrays
     * @param scheme the scheme to use for generating strings
     */
    class RepeatArrayAction(
        arrayScheme: ArrayScheme = ArraySettings.default.currentScheme,
        scheme: StringScheme = StringSettings.default.currentScheme
    ) : DataInsertRepeatArrayAction(ArrayAction(arrayScheme, scheme), RandomnessIcons.String.RepeatArray) {
        override val name = "Random Repeated String Array"
    }
}


/**
 * Controller for random string generation settings.
 *
 * @see StringSettings
 * @see StringSettingsComponent
 */
class StringSettingsAction : DataSettingsAction(RandomnessIcons.String.Settings) {
    override val name = "String Settings"

    override val configurableClass = StringSettingsConfigurable::class.java


    /**
     * Opens a popup to allow the user to quickly switch to the selected scheme.
     *
     * @param settings the settings containing the schemes that can be switched between
     */
    class StringQuickSwitchSchemeAction(settings: StringSettings = StringSettings.default) :
        DataQuickSwitchSchemeAction<StringScheme>(settings, RandomnessIcons.String.QuickSwitchScheme) {
        override val name = "Quick Switch String Scheme"
    }
}
