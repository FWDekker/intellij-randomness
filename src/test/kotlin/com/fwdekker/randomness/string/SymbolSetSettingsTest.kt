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

        it("does not deserialize pre-serialized emoji") {
            val settings = SymbolSetSettings().also { it.symbolSets = listOf(SymbolSet("emoji", ":massage:")) }

            assertThat(settings.symbolSets.single { it.name == "emoji" }.symbols).isEqualTo(":massage:")
        }

        it("does not deserialize pre-serialized emoji given a preceding first backslash") {
            val settings = SymbolSetSettings().also { it.symbolSets = listOf(SymbolSet("emoji", "\\:massage:")) }

            assertThat(settings.symbolSets.single { it.name == "emoji" }.symbols).isEqualTo("\\:massage:")
        }

        it("does not deserialize pre-serialized emoji given a preceding second backslash") {
            val settings = SymbolSetSettings().also { it.symbolSets = listOf(SymbolSet("emoji", ":massage\\:")) }

            assertThat(settings.symbolSets.single { it.name == "emoji" }.symbols).isEqualTo(":massage\\:")
        }

        it("does not deserialize pre-serialized emoji given multiple preceding backslashes") {
            val settings = SymbolSetSettings().also { it.symbolSets = listOf(SymbolSet("emoji", "\\:massage\\:")) }

            assertThat(settings.symbolSets.single { it.name == "emoji" }.symbols).isEqualTo("\\:massage\\:")
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

            assertThat(SymbolSetSettings(symbolSets).doValidate()).isEqualTo("Assign a name to symbol set at index 0.")
        }

        it("fails if two symbol sets have the same name") {
            val symbolSets = listOf(SymbolSet("seldom", "K0A6pdHk"), SymbolSet("seldom", "sllfXObM"))

            assertThat(SymbolSetSettings(symbolSets).doValidate())
                .isEqualTo("Symbol set names should be unique. Rename symbol set 'seldom'.")
        }

        it("fails if a symbol set has no symbols") {
            val symbolSets = listOf(SymbolSet("value", ""))

            assertThat(SymbolSetSettings(symbolSets).doValidate())
                .isEqualTo("Add at least one symbol to symbol set 'value'.")
        }
    }
})
