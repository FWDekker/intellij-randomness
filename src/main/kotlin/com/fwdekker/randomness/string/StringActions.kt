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
 * Generates random alphanumerical strings based on the settings in [StringSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [StringSettings.default]
 */
class StringInsertAction(private val settings: StringSettings = StringSettings.default) : DataInsertAction() {
    override val name = "Insert String"


    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    override fun generateString(): String {
        val length = Random.nextInt(settings.minLength, settings.maxLength + 1)

        val text = List(length) { generateCharacter() }.joinToString("")
        val capitalizedText = settings.capitalization.transform(text)

        return settings.enclosure + capitalizedText + settings.enclosure
    }


    /**
     * Returns a random character from the alphabet.
     *
     * @return a random character from the alphabet
     */
    private fun generateCharacter(): Char {
        val alphabet = Alphabet.concatenate(settings.alphabets)
        val charIndex = Random.nextInt(alphabet.length)

        return alphabet[charIndex]
    }
}


/**
 * Inserts an array of strings.
 */
class StringInsertArrayAction(settings: StringSettings = StringSettings.default) :
    DataInsertArrayAction(StringInsertAction(settings)) {
    override val name = "Insert String Array"
}


/**
 * Controller for random string generation settings.
 */
class StringSettingsAction : SettingsAction() {
    override val title = "String Settings"


    public override fun createDialog() = StringSettingsDialog()
}
