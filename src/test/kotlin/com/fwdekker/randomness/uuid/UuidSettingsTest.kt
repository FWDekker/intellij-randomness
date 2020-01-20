package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


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

    describe("copying") {
        describe("copyFrom") {
            it("makes the two schemes equal") {
                val schemeA = UuidScheme()
                val schemeB = UuidScheme(myName = "Name")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeA.copyFrom(schemeB)

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }

        describe("copyAs") {
            it("makes two schemes equal except for the name") {
                val schemeA = UuidScheme()
                val schemeB = schemeA.copyAs("NewName")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeB.myName = schemeA.myName

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }
    }
})
