package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [DecimalInsertAction].
 */
class DecimalInsertActionTest {
    companion object {
        @JvmStatic
        private fun provider() =
            listOf(
                // Zero decimal places
                arrayOf(5.0, 0, true, "5"),
                arrayOf(5.0, 1, true, "5.0"),
                arrayOf(5.0, 2, true, "5.00"),
                arrayOf(5.0, 0, false, "5"),
                arrayOf(5.0, 1, false, "5"),
                arrayOf(5.0, 2, false, "5"),
                // One decimal place
                arrayOf(47.6, 0, true, "48"),
                arrayOf(47.6, 1, true, "47.6"),
                arrayOf(47.6, 2, true, "47.60"),
                arrayOf(47.6, 0, false, "48"),
                arrayOf(47.6, 1, false, "47.6"),
                arrayOf(47.6, 2, false, "47.6"),
                // Two decimal places
                arrayOf(79.59, 0, true, "80"),
                arrayOf(79.59, 1, true, "79.6"),
                arrayOf(79.59, 2, true, "79.59"),
                arrayOf(79.59, 3, true, "79.590"),
                arrayOf(79.59, 0, false, "80"),
                arrayOf(79.59, 1, false, "79.6"),
                arrayOf(79.59, 2, false, "79.59"),
                arrayOf(79.59, 3, false, "79.59"),
                // Negative numbers
                arrayOf(-85.71, 0, true, "-86"),
                arrayOf(-85.71, 1, true, "-85.7"),
                arrayOf(-85.71, 2, true, "-85.71"),
                arrayOf(-85.71, 3, true, "-85.710"),
                arrayOf(-85.71, 0, false, "-86"),
                arrayOf(-85.71, 1, false, "-85.7"),
                arrayOf(-85.71, 2, false, "-85.71"),
                arrayOf(-85.71, 3, false, "-85.71")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(value: Double, decimalCount: Int, showTrailingZeroes: Boolean, expectedString: String) {
        val decimalScheme = DecimalScheme()
        decimalScheme.minValue = value
        decimalScheme.maxValue = value
        decimalScheme.decimalCount = decimalCount
        decimalScheme.showTrailingZeroes = showTrailingZeroes

        val insertRandomDecimal = DecimalInsertAction(decimalScheme)
        val randomString = insertRandomDecimal.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }

    @Test
    fun testInvalidRange() {
        val action = DecimalInsertAction(DecimalScheme("Default", 365.85, 241.54))
        assertThatThrownBy { action.generateString() }
            .isInstanceOf(DataGenerationException::class.java)
            .hasMessage("Minimum value is larger than maximum value.")
    }
}
