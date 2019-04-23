package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test


/**
 * Unit tests for [JLongSpinner].
 */
class JLongSpinnerTest {
    @Test
    fun testIllegalRange() {
        assertThatThrownBy { JLongSpinner(414, 989, -339) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("(minimum <= value <= maximum) is false")
    }


    @Test
    fun testGetSetValue() {
        val spinner = JLongSpinner()

        spinner.value = -583L

        assertThat(spinner.value).isEqualTo(-583L)
    }

    @Test
    fun testGetSetValueType() {
        val spinner = JLongSpinner()

        spinner.value = 125

        assertThat(spinner.value).isEqualTo(125L)
    }

    @Test
    fun testGetSetValueTruncation() {
        val spinner = JLongSpinner()

        spinner.setValue(786.79)

        assertThat(spinner.value).isEqualTo(786L)
    }


    @Test
    fun testGetSetMinValue() {
        val spinner = JLongSpinner()

        spinner.minValue = 979L

        assertThat(spinner.minValue).isEqualTo(979L)
    }

    @Test
    fun testGetSetMaxValue() {
        val spinner = JLongSpinner()

        spinner.maxValue = 166L

        assertThat(spinner.maxValue).isEqualTo(166L)
    }


    @Test
    fun testValidateUnderflowCustomRange() {
        val spinner = JLongSpinner(-665, -950, -559)

        spinner.value = -979

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -950.")
    }

    @Test
    fun testValidateOverflowCustomRange() {
        val spinner = JLongSpinner(424, 279, 678)

        spinner.value = 838

        val info = spinner.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("Please enter a value less than or equal to 678.")
    }
}
