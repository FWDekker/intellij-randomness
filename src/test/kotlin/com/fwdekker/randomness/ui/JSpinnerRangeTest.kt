package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.swing.JSpinner


/**
 * Unit tests for [JSpinnerRange].
 */
class JSpinnerRangeTest {
    private lateinit var min: JSpinner
    private lateinit var max: JSpinner


    @BeforeEach
    fun beforeEach() {
        min = mock(JSpinner::class.java)
        max = mock(JSpinner::class.java)
    }


    @Test
    fun testIllegalMaxRange() {
        assertThatThrownBy { JSpinnerRange(min, max, -37.20) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("maxRange must be a positive number.")
    }


    @Test
    fun testRangeRelative() {
        `when`(min.value).thenReturn(85.20)
        `when`(max.value).thenReturn(-636.33)

        val range = JSpinnerRange(min, max, JSpinnerRange.DEFAULT_MAX_RANGE)

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The maximum should be no smaller than the minimum.")
    }

    @Test
    fun testRangeSize() {
        `when`(min.value).thenReturn(-1E53)
        `when`(max.value).thenReturn(1E53)

        val range = JSpinnerRange(min, max, JSpinnerRange.DEFAULT_MAX_RANGE)

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The range should not exceed 1.0E53.")
    }

    @Test
    fun testRangeSizeCustomRange() {
        `when`(min.value).thenReturn(-794.90)
        `when`(max.value).thenReturn(769.52)

        val range = JSpinnerRange(min, max, 793.31)

        val info = range.validateValue()
        assertThat(info).isNotNull()
        assertThat(info?.message).isEqualTo("The range should not exceed 793.31.")
    }
}
