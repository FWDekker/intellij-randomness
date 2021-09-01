package com.fwdekker.randomness.string

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [SymbolSetSettings].
 */
object SymbolSetSettingsTest : Spek({
    describe("emoji compatibility") {
        it("serializes emoji") {
            val settings = SymbolSetSettings().also { it.symbolSets = listOf(SymbolSet("emoji", "💆")) }

            assertThat(settings.serializedSymbolSets.single { it.name == "emoji" }.symbols).isEqualTo(":massage:")
        }

        it("deserializes emoji") {
            val settings = SymbolSetSettings(listOf(SymbolSet("emoji", ":couple_with_heart_man_man:")))

            assertThat(settings.symbolSets.single { it.name == "emoji" }.symbols).isEqualTo("👨‍❤️‍👨")
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(SymbolSetSettings().doValidate()).isNull()
        }

        it("fails if no symbol sets are defined") {
            assertThat(SymbolSetSettings(emptyList()).doValidate()).isEqualTo("Add at least one symbol set.")
        }

        it("fails if a symbol set does not have a name") {
            val symbolSets = listOf(SymbolSet("", "hAA76o"))

            assertThat(SymbolSetSettings(symbolSets).doValidate())
                .isEqualTo("All symbol sets should have a name.")
        }

        it("fails if two symbol sets have the same name") {
            val symbolSets = listOf(SymbolSet("seldom", "K0A6pdHk"), SymbolSet("seldom", "sllfXObM"))

            assertThat(SymbolSetSettings(symbolSets).doValidate())
                .isEqualTo("Multiple symbol sets with name 'seldom'.")
        }

        it("fails if a symbol set has no symbols") {
            val symbolSets = listOf(SymbolSet("value", ""))

            assertThat(SymbolSetSettings(symbolSets).doValidate())
                .isEqualTo("Symbol set `value` should contain at least one symbol.")
        }
    }
})
