package com.fwdekker.randomness.decimal

import com.fwdekker.randomness.DataGenerationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy


/**
 * Unit tests for [DecimalScheme].
 */
object DecimalSchemeTest : DescribeSpec({
    lateinit var decimalScheme: DecimalScheme


    beforeEach {
        decimalScheme = DecimalScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            decimalScheme.decimalCount = -12

            assertThatThrownBy { decimalScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        describe("value range") {
            it("generates values within the specified range") {
                forAll(
                    table(
                        headers("value", "decimal count", "show trailing zeroes", "expected string"),
                        // Zero decimal places
                        row(5.0, 0, true, "5"),
                        row(5.0, 1, true, "5.0"),
                        row(5.0, 2, true, "5.00"),
                        row(5.0, 0, false, "5"),
                        row(5.0, 1, false, "5"),
                        row(5.0, 2, false, "5"),
                        // One decimal place
                        row(47.6, 0, true, "48"),
                        row(47.6, 1, true, "47.6"),
                        row(47.6, 2, true, "47.60"),
                        row(47.6, 0, false, "48"),
                        row(47.6, 1, false, "47.6"),
                        row(47.6, 2, false, "47.6"),
                        // Two decimal places
                        row(79.59, 0, true, "80"),
                        row(79.59, 1, true, "79.6"),
                        row(79.59, 2, true, "79.59"),
                        row(79.59, 3, true, "79.590"),
                        row(79.59, 0, false, "80"),
                        row(79.59, 1, false, "79.6"),
                        row(79.59, 2, false, "79.59"),
                        row(79.59, 3, false, "79.59"),
                        // Negative numbers
                        row(-85.71, 0, true, "-86"),
                        row(-85.71, 1, true, "-85.7"),
                        row(-85.71, 2, true, "-85.71"),
                        row(-85.71, 3, true, "-85.710"),
                        row(-85.71, 0, false, "-86"),
                        row(-85.71, 1, false, "-85.7"),
                        row(-85.71, 2, false, "-85.71"),
                        row(-85.71, 3, false, "-85.71"),
                    )
                ) { value, decimalCount, showTrailingZeroes, expectedString ->
                    decimalScheme.minValue = value
                    decimalScheme.maxValue = value
                    decimalScheme.decimalCount = decimalCount
                    decimalScheme.showTrailingZeroes = showTrailingZeroes

                    assertThat(decimalScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("separator") {
            it("generates values with the specified separators") {
                forAll(
                    table(
                        headers("value", "decimal count", "grouping separator", "decimal separator", "expected string"),
                        // Decimal separator only
                        row(4.21, 2, "", ".", "4.21"),
                        row(4.21, 2, "", ",", "4,21"),
                        row(4.21, 2, "", ".", "4.21"),
                        row(4.21, 2, "", ",", "4,21"),
                        // Grouping separator only
                        row(15_616.0, 0, ".", ".", "15.616"),
                        row(15_616.0, 0, ".", ",", "15.616"),
                        row(15_616.0, 0, ",", ".", "15,616"),
                        row(15_616.0, 0, ",", ",", "15,616"),
                        // Both separators
                        row(67_575.845, 3, "", ".", "67575.845"),
                        row(67_575.845, 3, ".", ".", "67.575.845"),
                        row(67_575.845, 3, ".", ",", "67.575,845"),
                        row(67_575.845, 3, ",", ".", "67,575.845"),
                        row(67_575.845, 3, ",", ",", "67,575,845"),
                    )
                ) { value, decimalCount, groupingSeparator, decimalSeparator, expectedString ->
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
            it("generates values with the specified prefix and suffix") {
                forAll(
                    table(
                        headers("value", "prefix", "suffix", "expected string"),
                        row(922.86, "", "", "922.86"),
                        row(136.50, "before", "after", "before136.50after"),
                        row(941.07, "\\0", "", "\\0941.07"),
                        row(693.27, "", "f", "693.27f"),
                    )
                ) { value, prefix, suffix, expectedString ->
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

        it("fails if the decorator is invalid") {
            decimalScheme.arrayDecorator.minCount = -284

            assertThat(decimalScheme.doValidate()).isNotNull()
        }

        describe("value range") {
            it("fails if the minimum value is larger than the maximum value") {
                decimalScheme.minValue = 395.0
                decimalScheme.maxValue = 264.0

                assertThat(decimalScheme.doValidate())
                    .isEqualTo("Minimum value should be less than or equal to maximum value.")
            }

            it("fails if the range size overflows") {
                decimalScheme.minValue = -1E53
                decimalScheme.maxValue = 1E53

                assertThat(decimalScheme.doValidate())
                    .isEqualTo("Minimum and maximum value should not differ by more than 1.0E53.")
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

        describe("separators") {
            it("fails if the grouping separator has multiple characters") {
                decimalScheme.groupingSeparator = "tce"

                assertThat(decimalScheme.doValidate()).isEqualTo("Grouping separator must be at most 1 character.")
            }

            it("fails if no decimal separator was selected") {
                decimalScheme.decimalSeparator = ""

                assertThat(decimalScheme.doValidate()).isEqualTo("Decimal separator must be exactly 1 character.")
            }

            it("fails if the decimal separator has multiple characters") {
                decimalScheme.decimalSeparator = "ned"

                assertThat(decimalScheme.doValidate()).isEqualTo("Decimal separator must be exactly 1 character.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            decimalScheme.minValue = 613.24
            decimalScheme.arrayDecorator.minCount = 926

            val copy = decimalScheme.deepCopy()
            copy.minValue = 10.21
            copy.arrayDecorator.minCount = 983

            assertThat(decimalScheme.minValue).isEqualTo(613.24)
            assertThat(decimalScheme.arrayDecorator.minCount).isEqualTo(926)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            decimalScheme.minValue = 399.75
            decimalScheme.maxValue = 928.22
            decimalScheme.decimalCount = 205
            decimalScheme.showTrailingZeroes = false
            decimalScheme.groupingSeparator = "a"
            decimalScheme.customGroupingSeparator = "2"
            decimalScheme.decimalSeparator = "D"
            decimalScheme.customDecimalSeparator = "P"
            decimalScheme.prefix = "baby"
            decimalScheme.suffix = "many"
            decimalScheme.arrayDecorator.minCount = 19

            val newScheme = DecimalScheme()
            newScheme.copyFrom(decimalScheme)

            assertThat(newScheme)
                .isEqualTo(decimalScheme)
                .isNotSameAs(decimalScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(decimalScheme.arrayDecorator)
                .isNotSameAs(decimalScheme.arrayDecorator)
        }
    }
})
