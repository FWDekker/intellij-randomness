package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [StringScheme].
 */
object StringSchemeTest : Spek({
    lateinit var stringScheme: StringScheme


    beforeEachTest {
        stringScheme = StringScheme(SymbolSetSettings())
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            stringScheme.minLength = 99
            stringScheme.maxLength = 21

            assertThatThrownBy { stringScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        describe("pattern") {
            data class Param(
                val minLength: Int,
                val maxLength: Int,
                val enclosure: String,
                val capitalization: CapitalizationMode,
                val symbolSets: Map<String, String>
            )

            mapOf(
                Param(1, 1, "", CapitalizationMode.RETAIN, mapOf("x" to "x")) to "x",
                Param(1, 1, "'", CapitalizationMode.UPPER, mapOf("x" to "x")) to "'X'",
                Param(1, 1, "a", CapitalizationMode.LOWER, mapOf("x" to "x")) to "axa",
                Param(1, 1, "2Rv", CapitalizationMode.FIRST_LETTER, mapOf("x" to "x")) to "2RvX2Rv",
                Param(723, 723, "", CapitalizationMode.UPPER, mapOf("x" to "x")) to "X".repeat(723),
                Param(466, 466, "z", CapitalizationMode.LOWER, mapOf("x" to "x")) to "z${"x".repeat(466)}z"
            ).forEach { (minLength, maxLength, enclosure, capitalization, symbolSets), expectedString ->
                it("generates a formatted string") {
                    stringScheme.symbolSetSettings = SymbolSetSettings().also { it.symbolSets = symbolSets }
                    stringScheme.minLength = minLength
                    stringScheme.maxLength = maxLength
                    stringScheme.enclosure = enclosure
                    stringScheme.capitalization = capitalization
                    stringScheme.activeSymbolSets = symbolSets.values.toSet()

                    assertThat(stringScheme.generateStrings()).containsExactly(expectedString)
                }
            }

            it("retains emoji modifiers in the right order") {
                val emoji = "👩‍👩‍👧‍👧"
                val symbolSets = mapOf("emoji" to emoji)

                stringScheme.symbolSetSettings = SymbolSetSettings().also { it.symbolSets = symbolSets }
                stringScheme.minLength = 1
                stringScheme.maxLength = 1
                stringScheme.enclosure = ""
                stringScheme.capitalization = CapitalizationMode.RETAIN
                stringScheme.activeSymbolSets = symbolSets.keys.toSet()

                assertThat(stringScheme.generateStrings()).containsExactly(emoji)
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(StringScheme(SymbolSetSettings()).doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                stringScheme.minLength = -161

                assertThat(stringScheme.doValidate()).isEqualTo("Minimum length should not be smaller than 1.")
            }
        }

        describe("symbol sets") {
            it("fails if there are no symbol sets") {
                stringScheme.symbolSetSettings = SymbolSetSettings(emptyMap())
                stringScheme.activeSymbolSets = emptySet()

                assertThat(stringScheme.doValidate()).isEqualTo("Add at least one symbol set.")
            }

            it("fails if a symbol set does not have a name") {
                stringScheme.symbolSetSettings = SymbolSetSettings(mapOf("" to "abc"))
                stringScheme.activeSymbolSets = setOf("")

                assertThat(stringScheme.doValidate()).isEqualTo("All symbol sets should have a name.")
            }

            it("fails if a symbol set does not have symbols") {
                stringScheme.symbolSetSettings = SymbolSetSettings(mapOf("name" to ""))
                stringScheme.activeSymbolSets = setOf("name")

                assertThat(stringScheme.doValidate()).isEqualTo("Symbol set `name` should contain at least one symbol.")
            }

            it("fails if no symbol sets are active") {
                stringScheme.activeSymbolSets = emptySet()

                assertThat(stringScheme.doValidate()).isEqualTo("Activate at least one symbol set.")
            }

            it("fails if only look-alike symbols are selected and look-alike symbols are excluded") {
                stringScheme.symbolSetSettings = SymbolSetSettings(mapOf("Look-alike" to "l01"))
                stringScheme.activeSymbolSets = setOf("Look-alike")
                stringScheme.excludeLookAlikeSymbols = true

                assertThat(stringScheme.doValidate()).isEqualTo(
                    "Active symbol sets should contain at least one non-look-alike character if look-alike " +
                        "characters are excluded."
                )
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            stringScheme.minLength = 49

            val copy = stringScheme.deepCopy()
            copy.minLength = 244

            assertThat(stringScheme.minLength).isEqualTo(49)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            val symbolSets = setOf(SymbolSet.BRACKETS.name)

            stringScheme.minLength = 730
            stringScheme.maxLength = 891
            stringScheme.enclosure = "Qh7"
            stringScheme.activeSymbolSets = symbolSets
            stringScheme.excludeLookAlikeSymbols = true

            val newScheme = StringScheme(SymbolSetSettings())
            newScheme.copyFrom(stringScheme)

            assertThat(newScheme).isEqualTo(stringScheme)
            assertThat(newScheme).isNotSameAs(stringScheme)
        }
    }
})
