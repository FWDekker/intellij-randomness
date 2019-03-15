package com.fwdekker.randomness.string

import com.fwdekker.randomness.DataInsertAction
import java.util.concurrent.ThreadLocalRandom


/**
 * Generates random alphanumerical strings based on the settings in [StringSettings].
 *
 * @param settings the settings to use for generating integers. Defaults to [StringSettings.instance]
 */
class StringInsertAction(private val settings: StringSettings = StringSettings.instance) : DataInsertAction() {
    override fun getName() = "Insert String"


    /**
     * Returns a random string of alphanumerical characters.
     *
     * @return a random string of alphanumerical characters
     */
    public override fun generateString(): String {
        val length = ThreadLocalRandom.current()
            .nextInt(settings.minLength, settings.maxLength + 1)

        val text = CharArray(length)
        for (i in 0 until length) {
            text[i] = generateCharacter()
        }

        val capitalizedText = settings.capitalization.transform.apply(String(text))
        return settings.enclosure + capitalizedText + settings.enclosure
    }


    /**
     * Returns a random character from the alphabet.
     *
     * @return a random character from the alphabet
     */
    private fun generateCharacter(): Char {
        val alphabet = Alphabet.concatenate(settings.alphabets)
        val charIndex = ThreadLocalRandom.current().nextInt(alphabet.length)

        return alphabet[charIndex]
    }


    /**
     * Inserts an array of strings.
     */
    inner class ArrayAction : DataInsertAction.ArrayAction(this@StringInsertAction) {
        override fun getName() = "Insert String Array"
    }
}
