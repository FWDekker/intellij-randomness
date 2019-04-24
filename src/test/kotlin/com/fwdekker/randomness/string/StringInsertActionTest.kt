package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.regex.Pattern


/**
 * Parameterized unit tests for [StringInsertAction].
 */
class StringInsertActionTest {
    companion object {
        @JvmStatic
        fun provider() =
            listOf(
                arrayOf(0, 0, "", CapitalizationMode.RETAIN, setOf<Alphabet>()),
                arrayOf(0, 0, "'", CapitalizationMode.UPPER, setOf<Alphabet>()),
                arrayOf(0, 0, "a", CapitalizationMode.LOWER, setOf<Alphabet>()),
                arrayOf(0, 0, "2Rv", CapitalizationMode.FIRST_LETTER, setOf<Alphabet>()),
                arrayOf(723, 723, "", CapitalizationMode.UPPER, setOf(Alphabet.ALPHABET)),
                arrayOf(466, 466, "z", CapitalizationMode.LOWER, setOf(Alphabet.ALPHABET, Alphabet.UNDERSCORE))
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(
        minLength: Int, maxLength: Int, enclosure: String,
        capitalization: CapitalizationMode, alphabets: Set<Alphabet>
    ) {
        val stringSettings = StringSettings()
        stringSettings.minLength = minLength
        stringSettings.maxLength = maxLength
        stringSettings.enclosure = enclosure
        stringSettings.capitalization = capitalization
        stringSettings.alphabets = alphabets.toMutableSet()

        val insertRandomString = StringInsertAction(stringSettings)
        val expectedPattern = buildExpectedPattern(minLength, maxLength, enclosure, capitalization, alphabets)

        assertThat(insertRandomString.generateString()).containsPattern(expectedPattern)
    }


    private fun buildExpectedPattern(
        minLength: Int, maxLength: Int, enclosure: String,
        capitalization: CapitalizationMode, alphabets: Set<Alphabet>
    ): Pattern {
        return Pattern.compile(
            "$enclosure${
            if (alphabets.isNotEmpty())
                "[${capitalization.transform.invoke(alphabets.sum())}]{$minLength,$maxLength}"
            else
                ""
            }$enclosure"
        )
    }
}
