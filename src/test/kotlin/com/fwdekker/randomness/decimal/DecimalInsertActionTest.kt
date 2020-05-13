package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupActionTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DecimalInsertAction].
 */
class DecimalInsertActionTest : Spek({
    describe("value range") {
        mapOf(
            // Zero decimal places
            Triple(5.0, 0, true) to "5",
            Triple(5.0, 1, true) to "5.0",
            Triple(5.0, 2, true) to "5.00",
            Triple(5.0, 0, false) to "5",
            Triple(5.0, 1, false) to "5",
            Triple(5.0, 2, false) to "5",
            // One decimal place
            Triple(47.6, 0, true) to "48",
            Triple(47.6, 1, true) to "47.6",
            Triple(47.6, 2, true) to "47.60",
            Triple(47.6, 0, false) to "48",
            Triple(47.6, 1, false) to "47.6",
            Triple(47.6, 2, false) to "47.6",
            // Two decimal places
            Triple(79.59, 0, true) to "80",
            Triple(79.59, 1, true) to "79.6",
            Triple(79.59, 2, true) to "79.59",
            Triple(79.59, 3, true) to "79.590",
            Triple(79.59, 0, false) to "80",
            Triple(79.59, 1, false) to "79.6",
            Triple(79.59, 2, false) to "79.59",
            Triple(79.59, 3, false) to "79.59",
            // Negative numbers
            Triple(-85.71, 0, true) to "-86",
            Triple(-85.71, 1, true) to "-85.7",
            Triple(-85.71, 2, true) to "-85.71",
            Triple(-85.71, 3, true) to "-85.710",
            Triple(-85.71, 0, false) to "-86",
            Triple(-85.71, 1, false) to "-85.7",
            Triple(-85.71, 2, false) to "-85.71",
            Triple(-85.71, 3, false) to "-85.71"
        ).forEach { (value, decimalCount, showTrailingZeroes), expectedString ->
            it("generates $expectedString") {
                val decimalScheme = DecimalScheme()
                decimalScheme.minValue = value
                decimalScheme.maxValue = value
                decimalScheme.decimalCount = decimalCount
                decimalScheme.showTrailingZeroes = showTrailingZeroes

                val insertRandomDecimal = DecimalInsertAction(decimalScheme)
                val randomString = insertRandomDecimal.generateString()

                assertThat(randomString).isEqualTo(expectedString)
            }
        }

        it("throws an exception of the minimum is larger than the maximum") {
            val action = DecimalInsertAction(DecimalScheme("Default", 365.85, 241.54))
            assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Minimum value is larger than maximum value.")
        }
    }

    describe("separator") {
        data class Param(
            val value: Double,
            val decimalCount: Int,
            val groupingSeparator: String,
            val decimalSeparator: String
        )

        mapOf(
            // Decimal separator only
            Param(4.21, 2, ".", ".") to "4.21",
            Param(4.21, 2, ".", ",") to "4,21",
            Param(4.21, 2, ",", ".") to "4.21",
            Param(4.21, 2, ",", ",") to "4,21",
            // Grouping separator only
            Param(15616.0, 0, ".", ".") to "15.616",
            Param(15616.0, 0, ".", ",") to "15.616",
            Param(15616.0, 0, ",", ".") to "15,616",
            Param(15616.0, 0, ",", ",") to "15,616",
            // Both separators
            Param(67575.845, 3, "", ".") to "67575.845",
            Param(67575.845, 3, ".", ".") to "67.575.845",
            Param(67575.845, 3, ".", ",") to "67.575,845",
            Param(67575.845, 3, ",", ".") to "67,575.845",
            Param(67575.845, 3, ",", ",") to "67,575,845"
        ).forEach { (value, decimalCount, groupingSeparator, decimalSeparator), expectedString ->
            it("generates $expectedString") {
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
    }

    describe("prefix and suffix") {
        mapOf(
            Triple(922.86, "", "") to "922.86",
            Triple(136.50, "before", "after") to "before136.50after",
            Triple(941.07, "\\0", "") to "\\0941.07",
            Triple(693.27, "", "f") to "693.27f"
        ).forEach { (value, prefix, suffix), expectedString ->
            it("generates $expectedString") {
                val decimalScheme = DecimalScheme(
                    minValue = value,
                    maxValue = value,
                    prefix = prefix,
                    suffix = suffix
                )

                val insertRandomDecimal = DecimalInsertAction(decimalScheme)
                val randomString = insertRandomDecimal.generateString()

                assertThat(randomString).isEqualTo(expectedString)
            }
        }
    }
})


/**
 * Unit tests for [DecimalGroupAction].
 */
class DecimalGroupActionTest : DataGroupActionTest({ DecimalGroupAction() })
