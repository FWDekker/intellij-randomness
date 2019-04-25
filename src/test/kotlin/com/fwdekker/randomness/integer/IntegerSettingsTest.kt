package com.fwdekker.randomness.integer

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [IntegerSettings].
 */
object IntegerSettingsTest : Spek({
    lateinit var integerSettings: IntegerSettings


    beforeEachTest {
        integerSettings = IntegerSettings()
    }


    describe("state persistence") {
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
})
