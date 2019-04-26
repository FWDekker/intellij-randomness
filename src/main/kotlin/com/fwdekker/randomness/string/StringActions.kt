package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataGroupAction
import com.fwdekker.randomness.DataInsertAction
import com.fwdekker.randomness.DataInsertArrayAction
import com.fwdekker.randomness.SettingsAction
import kotlin.random.Random


/**
 * All actions related to inserting strings.
 */
class StringGroupAction : DataGroupAction() {
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
class StringInsertAction(private val settings: StringSettings = StringSettings.default) : DataInsertAction() {
    override val name = "Insert String"


    /**
     * Returns strings of random alphanumerical characters.
     *
     * @param count the number of strings to generate
     * @return strings of random alphanumerical characters
     */
    override fun generateStrings(count: Int) =
        List(count) {
            val length = Random.nextInt(settings.minLength, settings.maxLength + 1)

            val text = List(length) { generateCharacter() }.joinToString("")
            val capitalizedText = settings.capitalization.transform(text)

            settings.enclosure + capitalizedText + settings.enclosure
        }


    /**
     * Returns a random character from the alphabets in `settings`.
     *
     * @return a random character from the alphabets in `settings`
     */
    private fun generateCharacter(): Char {
        val alphabet = settings.alphabets.sum()
        val charIndex = Random.nextInt(alphabet.length)

        return alphabet[charIndex]
    }
}


/**
 * Inserts an array-like string of strings.
 *
 * @param settings the settings to use for generating strings
 *
 * @see StringInsertAction
 */
class StringInsertArrayAction(settings: StringSettings = StringSettings.default) :
    DataInsertArrayAction(StringInsertAction(settings)) {
    override val name = "Insert String Array"
}


/**
 * Controller for random string generation settings.
 *
 * @see StringSettings
 * @see StringSettingsDialog
 */
class StringSettingsAction : SettingsAction() {
    override val title = "String Settings"


    public override fun createDialog() = StringSettingsDialog()
}
