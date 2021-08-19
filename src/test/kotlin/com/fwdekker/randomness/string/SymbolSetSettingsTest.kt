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
})
