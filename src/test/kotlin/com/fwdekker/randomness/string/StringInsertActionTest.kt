package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupActionTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [StringInsertAction].
 */
class StringInsertActionTest : Spek({
    describe("pattern") {
        data class Param(
            val minLength: Int,
            val maxLength: Int,
            val enclosure: String,
            val capitalization: CapitalizationMode,
            val symbolSets: Set<SymbolSet>
        )

        mapOf(
            Param(0, 0, "", CapitalizationMode.RETAIN, setOf(SymbolSet("x", "x"))) to "",
            Param(0, 0, "'", CapitalizationMode.UPPER, setOf(SymbolSet("x", "x"))) to "''",
            Param(0, 0, "a", CapitalizationMode.LOWER, setOf(SymbolSet("x", "x"))) to "aa",
            Param(0, 0, "2Rv", CapitalizationMode.FIRST_LETTER, setOf(SymbolSet("x", "x"))) to "2Rv2Rv",
            Param(723, 723, "", CapitalizationMode.UPPER, setOf(SymbolSet("x", "x"))) to "X".repeat(723),
            Param(466, 466, "z", CapitalizationMode.LOWER, setOf(SymbolSet("x", "x"))) to "z${"x".repeat(466)}z"
        ).forEach { (minLength, maxLength, enclosure, capitalization, symbolSets), expected ->
            it("generates a formatted string") {
                val stringScheme = StringScheme(
                    minLength = minLength,
                    maxLength = maxLength,
                    enclosure = enclosure,
                    capitalization = capitalization,
                    activeSymbolSets = symbolSets.toMap()
                )

                val insertRandomString = StringInsertAction(stringScheme)
                assertThat(insertRandomString.generateString()).isEqualTo(expected)
            }
        }

        it("retains emoji modifiers in the right order") {
            val emoji = "👩‍👩‍👧‍👧"
            val stringScheme = StringScheme(
                minLength = 1,
                maxLength = 1,
                enclosure = "",
                capitalization = CapitalizationMode.RETAIN,
                activeSymbolSets = setOf(SymbolSet("emoji", "👩‍👩‍👧‍👧")).toMap()
            )

            assertThat(StringInsertAction(stringScheme).generateString()).isEqualTo(emoji)
        }
    }

    describe("error handling") {
        it("throws an exception if the minimum is larger than the maximum") {
            val action = StringInsertAction(StringScheme(minLength = 99, maxLength = 21))
            Assertions.assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Minimum length is larger than maximum length.")
        }

        it("throws an exception if no valid symbols are found") {
            val action = StringInsertAction(StringScheme(activeSymbolSets = emptyMap()))
            Assertions.assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("No valid symbols found in active symbol sets.")
        }
    }
})


/**
 * Unit tests for [StringGroupAction].
 */
class StringGroupActionTest : DataGroupActionTest({ StringGroupAction() })
