package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Unit tests for the symbols used in [DecimalInsertAction].
 */
class DecimalInsertActionSymbolTest {
    companion object {
        @JvmStatic
        private fun provider() =
            listOf(
                arrayOf(4.2, 2, '.', '.', "4.20"),
                arrayOf(4.2, 2, '.', ',', "4,20"),
                arrayOf(4.2, 2, ',', '.', "4.20"),
                arrayOf(4.2, 2, ',', ',', "4,20"),
                arrayOf(67575.845, 3, '\u0000', '.', "67575.845"),
                arrayOf(67575.845, 3, '.', '.', "67.575.845"),
                arrayOf(67575.845, 3, '.', ',', "67.575,845"),
                arrayOf(67575.845, 3, ',', '.', "67,575.845"),
                arrayOf(67575.845, 3, ',', ',', "67,575,845")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(
        value: Double, decimalCount: Int, groupingSeparator: Char,
        decimalSeparator: Char, expectedString: String
    ) {
        val decimalSettings = DecimalSettings()
        decimalSettings.minValue = value
        decimalSettings.maxValue = value
        decimalSettings.decimalCount = decimalCount
        decimalSettings.groupingSeparator = groupingSeparator
        decimalSettings.decimalSeparator = decimalSeparator

        val insertRandomDecimal = DecimalInsertAction(decimalSettings)
        val randomString = insertRandomDecimal.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }
}
