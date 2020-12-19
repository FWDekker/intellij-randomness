package com.fwdekker.randomness.integer

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [IntegerSettings].
 */
object IntegerSettingsTest : Spek({
    lateinit var integerSettings: IntegerSettings


    beforeEachTest {
        integerSettings = IntegerSettings()
    }


    describe("copying") {
        it("creates an independent copy") {
            val copy = integerSettings.deepCopy()

            integerSettings.currentScheme.minValue = 159
            copy.currentScheme.minValue = 48

            assertThat(integerSettings.currentScheme.minValue).isEqualTo(159)
        }

        it("copies state from another instance") {
            integerSettings.currentScheme.minValue = 742
            integerSettings.currentScheme.maxValue = 908
            integerSettings.currentScheme.base = 12

            val newIntegerSettings = IntegerSettings()
            newIntegerSettings.loadState(integerSettings.state)

            assertThat(newIntegerSettings.currentScheme.minValue).isEqualTo(742)
            assertThat(newIntegerSettings.currentScheme.maxValue).isEqualTo(908)
            assertThat(newIntegerSettings.currentScheme.base).isEqualTo(12)
        }
    }
})


/**
 * Unit tests for [IntegerScheme].
 */
object IntegerSchemeTest : Spek({
    lateinit var integerScheme: IntegerScheme


    beforeEachTest {
        integerScheme = IntegerScheme()
    }


    describe("input handling") {
        describe("grouping separator") {
            it("uses the default separator if null is set") {
                integerScheme.safeSetGroupingSeparator(null)

                assertThat(integerScheme.groupingSeparator).isEqualTo(IntegerScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                integerScheme.safeSetGroupingSeparator("")

                assertThat(integerScheme.groupingSeparator).isEqualTo(IntegerScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                integerScheme.safeSetGroupingSeparator("recited")

                assertThat(integerScheme.groupingSeparator).isEqualTo("r")
            }
        }
    }

    describe("copying") {
        describe("copyFrom") {
            it("makes the two schemes equal") {
                val schemeA = IntegerScheme()
                val schemeB = IntegerScheme(myName = "Name")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeA.copyFrom(schemeB)

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }

        describe("copyAs") {
            it("makes two schemes equal except for the name") {
                val schemeA = IntegerScheme()
                val schemeB = schemeA.copyAs("NewName")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeB.myName = schemeA.myName

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }
    }
})
