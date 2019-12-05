package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.junit.jupiter.api.Test
import javax.swing.JSpinner


/**
 * Unit tests for [JSpinnerRange].
 */
class JSpinnerRangeTest {
    @Test
    fun testValidRange() {
        val range = JSpinnerRange(createJSpinner(287.01), createJSpinner(448.50), 758.34)

        assertThat(range.validateValue()).isNull()
    }

    @Test
    fun testIllegalMaxRange() {
        assertThatThrownBy { JSpinnerRange(createJSpinner(), createJSpinner(), -37.20) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("maxRange must be a positive number.")
    }


    @Test
    fun testRangeRelative() {
        val range = JSpinnerRange(createJSpinner(85.20), createJSpinner(-636.33))

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The maximum should not be smaller than the minimum.")
    }

    @Test
    fun testRangeSize() {
        val range = JSpinnerRange(createJSpinner(-1E53), createJSpinner(1E53))

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The range should not exceed 1.0E53.")
    }

    @Test
    fun testRangeSizeCustomRange() {
        val range = JSpinnerRange(createJSpinner(-794.90), createJSpinner(759.52), 793.31)

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The range should not exceed 793.31.")
    }

    @Test
    fun testName() {
        val range = JSpinnerRange(createJSpinner(459.18), createJSpinner(214.93), name="name")

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The maximum name should not be smaller than the minimum name.")
    }
}


private fun createJSpinner() =
    GuiActionRunner.execute<JSpinner> { JSpinner() }

private fun createJSpinner(value: Double) =
    GuiActionRunner.execute<JSpinner> { JSpinner().also { it.value = value } }
