package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
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
                arrayOf(5.0, 5.0, 0, "5"),
                arrayOf(7.0, 7.0, 1, "7.0"),
                arrayOf(8.0, 8.0, 2, "8.00"),
                arrayOf(47.6, 47.6, 0, "48"),
                arrayOf(56.4, 56.4, 1, "56.4"),
                arrayOf(73.7, 73.7, 2, "73.70"),
                arrayOf(21.85, 21.85, 0, "22"),
                arrayOf(79.59, 79.59, 1, "79.6"),
                arrayOf(43.51, 43.51, 2, "43.51"),
                arrayOf(83.82, 83.82, 3, "83.820"),
                arrayOf(77.592, 77.592, 0, "78"),
                arrayOf(14.772, 14.772, 1, "14.8"),
                arrayOf(98.602, 98.602, 2, "98.60"),
                arrayOf(32.675, 32.675, 3, "32.675"),
                arrayOf(52.800, 52.800, 4, "52.8000"),
                arrayOf(-22.252, -22.252, 0, "-22"),
                arrayOf(-85.703, -85.703, 1, "-85.7"),
                arrayOf(-52.686, -52.686, 2, "-52.69"),
                arrayOf(-94.202, -94.202, 3, "-94.202"),
                arrayOf(-60.152, -60.152, 4, "-60.1520")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(minValue: Double, maxValue: Double, decimalCount: Int, expectedString: String) {
        val decimalSettings = DecimalSettings()
        decimalSettings.minValue = minValue
        decimalSettings.maxValue = maxValue
        decimalSettings.decimalCount = decimalCount

        val insertRandomDecimal = DecimalInsertAction(decimalSettings)
        val randomString = insertRandomDecimal.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }
}
