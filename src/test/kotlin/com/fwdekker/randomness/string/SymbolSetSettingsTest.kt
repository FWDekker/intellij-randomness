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

            assertThat(settings.serializedSymbolSets.single { it.first == "emoji" }.second).isEqualTo(":massage:")
        }

        it("deserializes emoji") {
            val settings = SymbolSetSettings(listOf("emoji" to ":couple_with_heart_man_man:"))

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
            assertThat(SymbolSetSettings(listOf("" to "hAA76o")).doValidate())
                .isEqualTo("All symbol sets should have a name.")
        }

        it("fails if two symbol sets have the same name") {
            assertThat(SymbolSetSettings(listOf("seldom" to "K0A6pdHk", "seldom" to "sllfXObM")).doValidate())
                .isEqualTo("Multiple symbol sets with name 'seldom'.")
        }

        it("fails if a symbol set has no symbols") {
            assertThat(SymbolSetSettings(listOf("value" to "")).doValidate())
                .isEqualTo("Symbol set `value` should contain at least one symbol.")
        }
    }
})
