package com.fwdekker.randomness.integer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [IntegerSettings].
 */
class IntegerSettingsTest {
    private lateinit var integerSettings: IntegerSettings


    @BeforeEach
    fun beforeEach() {
        integerSettings = IntegerSettings()
    }


    @Test
    fun testGetLoadState() {
        integerSettings.minValue = 742
        integerSettings.maxValue = 908
        integerSettings.base = 12

        val newIntegerSettings = IntegerSettings()
        newIntegerSettings.loadState(integerSettings.state)

        assertThat(newIntegerSettings.minValue).isEqualTo(742)
        assertThat(newIntegerSettings.maxValue).isEqualTo(908)
        assertThat(newIntegerSettings.base).isEqualTo(12)
    }

    @Test
    fun testGetSetMinValue() {
        integerSettings.minValue = 366

        assertThat(integerSettings.minValue).isEqualTo(366)
    }

    @Test
    fun testGetSetMaxValue() {
        integerSettings.maxValue = 332

        assertThat(integerSettings.maxValue).isEqualTo(332)
    }

    @Test
    fun testGetSetBase() {
        integerSettings.base = 7

        assertThat(integerSettings.base).isEqualTo(7)
    }

    @Test
    fun testGetSetGroupingSeparator() {
        integerSettings.groupingSeparator = '6'

        assertThat(integerSettings.groupingSeparator).isEqualTo('6')
    }
}
