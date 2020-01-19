package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
                arrayOf(0, 0, "", CapitalizationMode.RETAIN, setOf<SymbolSet>()),
                arrayOf(0, 0, "'", CapitalizationMode.UPPER, setOf<SymbolSet>()),
                arrayOf(0, 0, "a", CapitalizationMode.LOWER, setOf<SymbolSet>()),
                arrayOf(0, 0, "2Rv", CapitalizationMode.FIRST_LETTER, setOf<SymbolSet>()),
                arrayOf(723, 723, "", CapitalizationMode.UPPER, setOf(SymbolSet.ALPHABET)),
                arrayOf(466, 466, "z", CapitalizationMode.LOWER, setOf(SymbolSet.ALPHABET, SymbolSet.UNDERSCORE))
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(
        minLength: Int, maxLength: Int, enclosure: String,
        capitalization: CapitalizationMode, symbolSets: Set<SymbolSet>
    ) {
        val stringScheme = StringScheme(
            minLength = minLength,
            maxLength = maxLength,
            enclosure = enclosure,
            capitalization = capitalization,
            activeSymbolSets = symbolSets.toMap()
        )

        val insertRandomString = StringInsertAction(stringScheme)
        val expectedPattern = buildExpectedPattern(minLength, maxLength, enclosure, capitalization, symbolSets)

        assertThat(insertRandomString.generateString()).containsPattern(expectedPattern)
    }

    @Test
    fun testInvalidRange() {
        val action = StringInsertAction(StringScheme(minLength = 99, maxLength = 21))
        Assertions.assertThatThrownBy { action.generateString() }
            .isInstanceOf(DataGenerationException::class.java)
            .hasMessage("Minimum length is larger than maximum length.")
    }

    @Test
    fun testNoSymbols() {
        val action = StringInsertAction(StringScheme(activeSymbolSets = emptyMap()))
        Assertions.assertThatThrownBy { action.generateString() }
            .isInstanceOf(DataGenerationException::class.java)
            .hasMessage("No valid symbols found in active symbol sets.")
    }


    private fun buildExpectedPattern(
        minLength: Int, maxLength: Int, enclosure: String,
        capitalization: CapitalizationMode, symbolSets: Set<SymbolSet>
    ): Pattern {
        return Pattern.compile(
            "$enclosure${
            if (symbolSets.isNotEmpty())
                "[${capitalization.transform.invoke(symbolSets.sum())}]{$minLength,$maxLength}"
            else
                ""
            }$enclosure"
        )
    }
}
