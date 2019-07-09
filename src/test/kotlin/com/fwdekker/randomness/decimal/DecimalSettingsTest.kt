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
            decimalSettings.showTrailingZeroes = false
            decimalSettings.groupingSeparator = "a"
            decimalSettings.decimalSeparator = "D"

            val newDecimalSettings = DecimalSettings()
            newDecimalSettings.loadState(decimalSettings.state)

            assertThat(newDecimalSettings.minValue).isEqualTo(399.75)
            assertThat(newDecimalSettings.maxValue).isEqualTo(928.22)
            assertThat(newDecimalSettings.decimalCount).isEqualTo(205)
            assertThat(newDecimalSettings.showTrailingZeroes).isEqualTo(false)
            assertThat(newDecimalSettings.groupingSeparator).isEqualTo("a")
            assertThat(newDecimalSettings.decimalSeparator).isEqualTo("D")
        }
    }

    describe("input handling") {
        describe("grouping separator") {
            it("uses the default separator if null is set") {
                decimalSettings.safeSetGroupingSeparator(null)

                assertThat(decimalSettings.groupingSeparator).isEqualTo(DecimalSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                decimalSettings.safeSetGroupingSeparator("")

                assertThat(decimalSettings.groupingSeparator).isEqualTo(DecimalSettings.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalSettings.safeSetGroupingSeparator("drummer")

                assertThat(decimalSettings.groupingSeparator).isEqualTo("d")
            }
        }

        describe("decimal separator") {
            it("uses the default separator if null is set") {
                decimalSettings.safeSetDecimalSeparator(null)

                assertThat(decimalSettings.decimalSeparator).isEqualTo(DecimalSettings.DEFAULT_DECIMAL_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                decimalSettings.safeSetDecimalSeparator("")

                assertThat(decimalSettings.decimalSeparator).isEqualTo(DecimalSettings.DEFAULT_DECIMAL_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalSettings.safeSetDecimalSeparator("foolish")

                assertThat(decimalSettings.decimalSeparator).isEqualTo("f")
            }
        }
    }
})
