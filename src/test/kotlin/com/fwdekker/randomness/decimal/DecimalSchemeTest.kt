package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [DecimalScheme].
 */
object DecimalSchemeTest : Spek({
    lateinit var decimalScheme: DecimalScheme


    beforeEachTest {
        decimalScheme = DecimalScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            decimalScheme.decimalCount = -12

            assertThatThrownBy { decimalScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

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
                    decimalScheme.minValue = value
                    decimalScheme.maxValue = value
                    decimalScheme.decimalCount = decimalCount
                    decimalScheme.showTrailingZeroes = showTrailingZeroes

                    assertThat(decimalScheme.generateStrings()).containsExactly(expectedString)
                }
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
                Param(15_616.0, 0, ".", ".") to "15.616",
                Param(15_616.0, 0, ".", ",") to "15.616",
                Param(15_616.0, 0, ",", ".") to "15,616",
                Param(15_616.0, 0, ",", ",") to "15,616",
                // Both separators
                Param(67_575.845, 3, "", ".") to "67575.845",
                Param(67_575.845, 3, ".", ".") to "67.575.845",
                Param(67_575.845, 3, ".", ",") to "67.575,845",
                Param(67_575.845, 3, ",", ".") to "67,575.845",
                Param(67_575.845, 3, ",", ",") to "67,575,845"
            ).forEach { (value, decimalCount, groupingSeparator, decimalSeparator), expectedString ->
                it("generates $expectedString") {
                    decimalScheme.minValue = value
                    decimalScheme.maxValue = value
                    decimalScheme.decimalCount = decimalCount
                    decimalScheme.showTrailingZeroes = false
                    decimalScheme.groupingSeparator = groupingSeparator
                    decimalScheme.decimalSeparator = decimalSeparator

                    assertThat(decimalScheme.generateStrings()).containsExactly(expectedString)
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
                    decimalScheme.minValue = value
                    decimalScheme.maxValue = value
                    decimalScheme.prefix = prefix
                    decimalScheme.suffix = suffix

                    assertThat(decimalScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(DecimalScheme().doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the range size overflows") {
                decimalScheme.minValue = -1E53
                decimalScheme.maxValue = 1E53

                assertThat(decimalScheme.doValidate()).isEqualTo("Value range should not exceed 1.0E53.")
            }
        }

        describe("decimal count") {
            it("passes if the decimal count is zero") {
                decimalScheme.decimalCount = 0

                assertThat(decimalScheme.doValidate()).isNull()
            }

            it("fails if the decimal count is negative") {
                decimalScheme.decimalCount = -851

                assertThat(decimalScheme.doValidate()).isEqualTo("Decimal count should be at least 0.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            val copy = decimalScheme.deepCopy()
            decimalScheme.minValue = 613.24
            copy.minValue = 10.21

            assertThat(decimalScheme.minValue).isEqualTo(613.24)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            decimalScheme.minValue = 399.75
            decimalScheme.maxValue = 928.22
            decimalScheme.decimalCount = 205
            decimalScheme.showTrailingZeroes = false
            decimalScheme.groupingSeparator = "a"
            decimalScheme.decimalSeparator = "D"
            decimalScheme.prefix = "baby"
            decimalScheme.suffix = "many"

            val newScheme = DecimalScheme()
            newScheme.copyFrom(decimalScheme)

            assertThat(newScheme).isEqualTo(decimalScheme)
            assertThat(newScheme).isNotSameAs(decimalScheme)
        }
    }
})
