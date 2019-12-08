package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [UuidSettings].
 */
object UuidSettingsTest : Spek({
    lateinit var uuidSettings: UuidSettings


    beforeEachTest {
        uuidSettings = UuidSettings()
    }


    describe("state management") {
        it("creates an independent copy") {
            val copy = uuidSettings.copyState()
            uuidSettings.version = 1
            uuidSettings.enclosure = "D"
            uuidSettings.capitalization = CapitalizationMode.RANDOM
            uuidSettings.addDashes = false
            copy.version = 4
            copy.enclosure = "p"
            copy.capitalization = CapitalizationMode.UPPER
            copy.addDashes = true

            assertThat(uuidSettings.version).isEqualTo(1)
            assertThat(uuidSettings.enclosure).isEqualTo("D")
            assertThat(uuidSettings.capitalization).isEqualTo(CapitalizationMode.RANDOM)
            assertThat(uuidSettings.addDashes).isEqualTo(false)
        }

        it("copies state from another instance") {
            uuidSettings.version = 4
            uuidSettings.enclosure = "nvpB"
            uuidSettings.capitalization = CapitalizationMode.FIRST_LETTER
            uuidSettings.addDashes = true

            val newUuidSettings = UuidSettings()
            newUuidSettings.loadState(uuidSettings.state)

            assertThat(newUuidSettings.version).isEqualTo(4)
            assertThat(newUuidSettings.enclosure).isEqualTo("nvpB")
            assertThat(newUuidSettings.capitalization).isEqualTo(CapitalizationMode.FIRST_LETTER)
            assertThat(newUuidSettings.addDashes).isEqualTo(true)
        }
    }
})
