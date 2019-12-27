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
            val copy = uuidSettings.deepCopy()
            uuidSettings.currentScheme.version = 1
            uuidSettings.currentScheme.enclosure = "D"
            uuidSettings.currentScheme.capitalization = CapitalizationMode.RANDOM
            uuidSettings.currentScheme.addDashes = false
            copy.currentScheme.version = 4
            copy.currentScheme.enclosure = "p"
            copy.currentScheme.capitalization = CapitalizationMode.UPPER
            copy.currentScheme.addDashes = true

            assertThat(uuidSettings.currentScheme.version).isEqualTo(1)
            assertThat(uuidSettings.currentScheme.enclosure).isEqualTo("D")
            assertThat(uuidSettings.currentScheme.capitalization).isEqualTo(CapitalizationMode.RANDOM)
            assertThat(uuidSettings.currentScheme.addDashes).isEqualTo(false)
        }

        it("copies state from another instance") {
            uuidSettings.currentScheme.version = 4
            uuidSettings.currentScheme.enclosure = "nvpB"
            uuidSettings.currentScheme.capitalization = CapitalizationMode.FIRST_LETTER
            uuidSettings.currentScheme.addDashes = true

            val newUuidSettings = UuidSettings()
            newUuidSettings.loadState(uuidSettings.state)

            assertThat(newUuidSettings.currentScheme.version).isEqualTo(4)
            assertThat(newUuidSettings.currentScheme.enclosure).isEqualTo("nvpB")
            assertThat(newUuidSettings.currentScheme.capitalization).isEqualTo(CapitalizationMode.FIRST_LETTER)
            assertThat(newUuidSettings.currentScheme.addDashes).isEqualTo(true)
        }
    }
})
