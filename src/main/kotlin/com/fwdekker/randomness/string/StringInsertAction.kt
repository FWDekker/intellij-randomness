package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataInsertAction
import kotlin.random.Random


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


    /**
     * Inserts an array of strings.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this) {
        override val name = "Insert String Array"
    }
}
