package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test


/**
 * Unit tests for [JDoubleSpinner].
 */
class JDoubleSpinnerTest {
    @Test
    fun testIllegalMinValue() {
        assertThatThrownBy { JDoubleSpinner(-1E80, -477.23) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("minValue should not be smaller than -1.0E53.")
    }

    @Test
    fun testIllegalMaxValue() {
        assertThatThrownBy { JDoubleSpinner(-161.29, 1E73) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("maxValue should not be greater than 1.0E53.")
    }

    @Test
    fun testIllegalRange() {
        assertThatThrownBy { JDoubleSpinner(-602.98, -929.41) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("minValue should be greater than maxValue.")
    }


    @Test
    fun testGetSetValue() {
        val spinner = JDoubleSpinner()

        spinner.value = 179.40

        assertThat(spinner.value).isEqualTo(179.40)
    }

    @Test
    fun testGetSetValueType() {
        val spinner = JDoubleSpinner()

        spinner.setValue(638L)

        assertThat(spinner.value).isEqualTo(638.0)
    }


    @Test
    fun testValidateUnderflow() {
        val spinner = JDoubleSpinner()

        spinner.value = -1E55

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -1.0E53.")
    }

    @Test
    fun testValidateOverflow() {
        val spinner = JDoubleSpinner()

        spinner.value = 1E98

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value less than or equal to 1.0E53.")
    }

    @Test
    fun testValidateUnderflowCustomRange() {
        val spinner = JDoubleSpinner(-738.33, 719.45)

        spinner.value = -808.68

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -738.33.")
    }

    @Test
    fun testValidateOverflowCustomRange() {
        val spinner = JDoubleSpinner(-972.80, -69.36)

        spinner.value = 94.0

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value less than or equal to -69.36.")
    }
}
