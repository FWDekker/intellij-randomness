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
            val settings = SymbolSetSettings().also { it.symbolSets = mapOf("emoji" to "💆") }

            assertThat(settings.serializedSymbolSets["emoji"]).isEqualTo(":massage:")
        }

        it("deserializes emoji") {
            val settings = SymbolSetSettings(mapOf("emoji" to ":couple_with_heart_man_man:"))

            assertThat(settings.symbolSets["emoji"]).isEqualTo("👨‍❤️‍👨")
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(SymbolSetSettings().doValidate()).isNull()
        }

        it("fails if no symbol sets are defined") {
            assertThat(SymbolSetSettings(emptyMap()).doValidate()).isEqualTo("Add at least one symbol set.")
        }

        it("fails if a symbol set does not have a name") {
            assertThat(SymbolSetSettings(mapOf("" to "hAA76o")).doValidate())
                .isEqualTo("All symbol sets should have a name.")
        }

        it("fails if a symbol set has no symbols") {
            assertThat(SymbolSetSettings(mapOf("value" to "")).doValidate())
                .isEqualTo("Symbol set `value` should contain at least one symbol.")
        }
    }
})
