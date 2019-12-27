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


    it("creates an independent copy") {
        val copy = decimalSettings.deepCopy()
        decimalSettings.currentScheme.minValue = 613.24
        copy.currentScheme.minValue = 10.21

        assertThat(decimalSettings.currentScheme.minValue).isEqualTo(613.24)
    }

    it("copies state from another instance") {
        decimalSettings.currentScheme.minValue = 399.75
        decimalSettings.currentScheme.maxValue = 928.22
        decimalSettings.currentScheme.decimalCount = 205
        decimalSettings.currentScheme.showTrailingZeroes = false
        decimalSettings.currentScheme.groupingSeparator = "a"
        decimalSettings.currentScheme.decimalSeparator = "D"

        val newDecimalSettings = DecimalSettings()
        newDecimalSettings.loadState(decimalSettings.state)

        assertThat(newDecimalSettings.currentScheme.minValue).isEqualTo(399.75)
        assertThat(newDecimalSettings.currentScheme.maxValue).isEqualTo(928.22)
        assertThat(newDecimalSettings.currentScheme.decimalCount).isEqualTo(205)
        assertThat(newDecimalSettings.currentScheme.showTrailingZeroes).isEqualTo(false)
        assertThat(newDecimalSettings.currentScheme.groupingSeparator).isEqualTo("a")
        assertThat(newDecimalSettings.currentScheme.decimalSeparator).isEqualTo("D")
    }
})


/**
 * Unit tests for [DecimalScheme].
 */
object DecimalSchemeTest : Spek({
    lateinit var decimalScheme: DecimalScheme


    beforeEachTest {
        decimalScheme = DecimalScheme()
    }


    describe("input handling") {
        describe("grouping separator") {
            it("uses the default separator if null is set") {
                decimalScheme.safeSetGroupingSeparator(null)

                assertThat(decimalScheme.groupingSeparator).isEqualTo(DecimalScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                decimalScheme.safeSetGroupingSeparator("")

                assertThat(decimalScheme.groupingSeparator).isEqualTo(DecimalScheme.DEFAULT_GROUPING_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalScheme.safeSetGroupingSeparator("drummer")

                assertThat(decimalScheme.groupingSeparator).isEqualTo("d")
            }
        }

        describe("decimal separator") {
            it("uses the default separator if null is set") {
                decimalScheme.safeSetDecimalSeparator(null)

                assertThat(decimalScheme.decimalSeparator).isEqualTo(DecimalScheme.DEFAULT_DECIMAL_SEPARATOR)
            }

            it("uses the default separator if an empty string is set") {
                decimalScheme.safeSetDecimalSeparator("")

                assertThat(decimalScheme.decimalSeparator).isEqualTo(DecimalScheme.DEFAULT_DECIMAL_SEPARATOR)
            }

            it("uses only the first character if a multi-character string is given") {
                decimalScheme.safeSetDecimalSeparator("foolish")

                assertThat(decimalScheme.decimalSeparator).isEqualTo("f")
            }
        }
    }
})
