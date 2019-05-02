package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [DecimalSettings].
 */
object DecimalSettingsTest : Spek({
    lateinit var decimalSettings: DecimalSettings


    beforeEachTest {
        decimalSettings = DecimalSettings()
    }


    describe("state persistence") {
        it("copies state from another instance") {
            decimalSettings.minValue = 399.75
            decimalSettings.maxValue = 928.22
            decimalSettings.decimalCount = 205

            val newDecimalSettings = DecimalSettings()
            newDecimalSettings.loadState(decimalSettings.state)

            assertThat(newDecimalSettings.minValue).isEqualTo(399.75)
            assertThat(newDecimalSettings.maxValue).isEqualTo(928.22)
            assertThat(newDecimalSettings.decimalCount).isEqualTo(205)
        }
    }

    describe("input handling") {
        describe("grouping separator") {
            it("uses the default separator if an empty string is set") {
                decimalSettings.groupingSeparator = ""

                assertThat(decimalSettings.groupingSeparator).isEqualTo(DecimalSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalSettings.groupingSeparator = "drummer"

                assertThat(decimalSettings.groupingSeparator).isEqualTo("d")
            }
        }

        describe("decimal separator") {
            it("uses the default separator if an empty string is set") {
                decimalSettings.decimalSeparator = ""

                assertThat(decimalSettings.decimalSeparator).isEqualTo(DecimalSettings.DEFAULT_DECIMAL_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalSettings.decimalSeparator = "foolish"

                assertThat(decimalSettings.decimalSeparator).isEqualTo("f")
            }
        }
    }
})
