package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Unit tests for [DecimalSettings].
 */
class DecimalSettingsTest {
    private lateinit var decimalSettings: DecimalSettings


    @BeforeEach
    fun beforeEach() {
        decimalSettings = DecimalSettings()
    }


    @Test
    fun testGetLoadState() {
        decimalSettings.minValue = 399.75
        decimalSettings.maxValue = 928.22
        decimalSettings.decimalCount = 205

        val newDecimalSettings = DecimalSettings()
        newDecimalSettings.loadState(decimalSettings.state)

        assertThat(newDecimalSettings.minValue).isEqualTo(399.75)
        assertThat(newDecimalSettings.maxValue).isEqualTo(928.22)
        assertThat(newDecimalSettings.decimalCount).isEqualTo(205)
    }

    @Test
    fun testGetSetMinValue() {
        decimalSettings.minValue = 720.41

        assertThat(decimalSettings.minValue).isEqualTo(720.41)
    }

    @Test
    fun testGetSetMaxValue() {
        decimalSettings.maxValue = 901.38

        assertThat(decimalSettings.maxValue).isEqualTo(901.38)
    }

    @Test
    fun testGetSetDecimalCount() {
        decimalSettings.decimalCount = 987

        assertThat(decimalSettings.decimalCount).isEqualTo(987)
    }

    @Test
    fun testGetSetGroupingSeparator() {
        decimalSettings.groupingSeparator = 'L'

        assertThat(decimalSettings.groupingSeparator).isEqualTo('L')
    }

    @Test
    fun testGetSetDecimalSeparator() {
        decimalSettings.decimalSeparator = '}'

        assertThat(decimalSettings.decimalSeparator).isEqualTo('}')
    }
}
