package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for the separators used in [DecimalInsertAction].
 */
class DecimalInsertActionSeparatorTest {
    companion object {
        @JvmStatic
        private fun provider() =
            listOf(
                // Decimal separator only
                arrayOf(4.21, 2, ".", ".", "4.21"),
                arrayOf(4.21, 2, ".", ",", "4,21"),
                arrayOf(4.21, 2, ",", ".", "4.21"),
                arrayOf(4.21, 2, ",", ",", "4,21"),
                // Grouping separator only
                arrayOf(15616, 0, ".", ".", "15.616"),
                arrayOf(15616, 0, ".", ",", "15.616"),
                arrayOf(15616, 0, ",", ".", "15,616"),
                arrayOf(15616, 0, ",", ",", "15,616"),
                // Both separators
                arrayOf(67575.845, 3, "", ".", "67575.845"),
                arrayOf(67575.845, 3, ".", ".", "67.575.845"),
                arrayOf(67575.845, 3, ".", ",", "67.575,845"),
                arrayOf(67575.845, 3, ",", ".", "67,575.845"),
                arrayOf(67575.845, 3, ",", ",", "67,575,845")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(
        value: Double, decimalCount: Int, groupingSeparator: String,
        decimalSeparator: String, expectedString: String
    ) {
        val decimalScheme = DecimalScheme(
            minValue = value,
            maxValue = value,
            decimalCount = decimalCount,
            showTrailingZeroes = false,
            groupingSeparator = groupingSeparator,
            decimalSeparator = decimalSeparator
        )

        val insertRandomDecimal = DecimalInsertAction(decimalScheme)
        val randomString = insertRandomDecimal.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }
}
