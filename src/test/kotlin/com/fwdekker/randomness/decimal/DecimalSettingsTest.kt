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
})
