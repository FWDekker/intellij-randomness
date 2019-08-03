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


    describe("state management") {
        it("creates an independent copy") {
            val copy = integerSettings.copyState()
            integerSettings.minValue = 159
            copy.minValue = 48

            assertThat(integerSettings.minValue).isEqualTo(159)
        }

        it("copies state from another instance") {
            integerSettings.minValue = 742
            integerSettings.maxValue = 908
            integerSettings.base = 12

            val newIntegerSettings = IntegerSettings()
            newIntegerSettings.loadState(integerSettings.state)

            assertThat(newIntegerSettings.minValue).isEqualTo(742)
            assertThat(newIntegerSettings.maxValue).isEqualTo(908)
            assertThat(newIntegerSettings.base).isEqualTo(12)
        }
    }

    describe("input handling") {
        describe("grouping separator") {
            it("uses the default separator if null is set") {
                integerSettings.safeSetGroupingSeparator(null)

                assertThat(integerSettings.groupingSeparator).isEqualTo(IntegerSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                integerSettings.safeSetGroupingSeparator("")

                assertThat(integerSettings.groupingSeparator).isEqualTo(IntegerSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                integerSettings.safeSetGroupingSeparator("recited")

                assertThat(integerSettings.groupingSeparator).isEqualTo("r")
            }
        }
    }
})
