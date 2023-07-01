package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy


/**
 * Unit tests for [IntegerScheme].
 */
object IntegerSchemeTest : DescribeSpec({
    lateinit var integerScheme: IntegerScheme


    beforeEach {
        integerScheme = IntegerScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            integerScheme.minValue = 65L
            integerScheme.maxValue = 24L

            assertThatThrownBy { integerScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        describe("value range") {
            it("generates values within the specified range") {
                forAll(
                    table(
                        headers("min value", "max value", "expected string"),
                        row(0L, 0L, "0"),
                        row(1L, 1L, "1"),
                        row(-5L, -5L, "-5"),
                        row(488L, 488L, "488"),
                        row(-876L, -876L, "-876"),
                        row(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE.toString()),
                        row(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE.toString()),
                    )
                ) { minValue, maxValue, expectedString ->
                    integerScheme.minValue = minValue
                    integerScheme.maxValue = maxValue

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }

            it("generates a random value at maximum range size") {
                integerScheme.minValue = Long.MIN_VALUE
                integerScheme.maxValue = Long.MAX_VALUE

                // Passes with extremely high probability (p = 1 - (2/(2^64))
                assertThat(integerScheme.generateStrings())
                    .isNotEqualTo(Long.MIN_VALUE.toString())
                    .isNotEqualTo(Long.MAX_VALUE.toString())
            }
        }

        describe("base") {
            it("generates values in the specified base") {
                forAll(
                    table(
                        headers("value", "base", "grouping separator", "expected string"),
                        row(33_360L, 10, ".", "33.360"),
                        row(48_345L, 10, ".", "48.345"),
                        row(48_345L, 11, ".", "33360"),
                    )
                ) { value, base, groupingSeparator, expectedString ->
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.base = base
                    integerScheme.groupingSeparator = groupingSeparator

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("separator") {
            it("generates strings with the specified separator") {
                forAll(
                    table(
                        headers("value", "grouping separator", "expected string"),
                        row(95_713L, "", "95713"),
                        row(163_583L, ".", "163.583"),
                        row(351_426L, ",", "351,426"),
                    )
                ) { value, groupingSeparator, expectedString ->
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.groupingSeparator = groupingSeparator

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("capitalization") {
            it("generates strings with the specified capitalization") {
                forAll(
                    table(
                        headers("value", "base", "prefix", "capitalization", "expected string"),
                        row(624L, 10, "", CapitalizationMode.UPPER, "624"),
                        row(254L, 16, "", CapitalizationMode.UPPER, "FE"),
                        row(254L, 16, "0x", CapitalizationMode.FIRST_LETTER, "0xFe"),
                    )
                ) { value, base, prefix, capitalization, expectedString ->
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.base = base
                    integerScheme.prefix = prefix
                    integerScheme.capitalization = capitalization

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("prefix and suffix") {
            it("generates strings with the specified prefix and suffix") {
                forAll(
                    table(
                        headers("value", "prefix", "suffix", "expected string"),
                        row(920L, "", "", "920"),
                        row(553L, "before", "after", "before553after"),
                        row(948L, "0x", "", "0x948"),
                        row(502L, "", "L", "502L"),
                    )
                ) { value, prefix, suffix, expectedString ->
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.prefix = prefix
                    integerScheme.suffix = suffix
                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(IntegerScheme().doValidate()).isNull()
        }

        it("fails if the fixed-length decorator is invalid") {
            integerScheme.fixedLengthDecorator.length = -45

            assertThat(integerScheme.doValidate()).isNotNull()
        }

        it("fails if the array decorator is invalid") {
            integerScheme.arrayDecorator.minCount = -584

            assertThat(integerScheme.doValidate()).isNotNull()
        }

        describe("base") {
            it("fails if the base is negative") {
                integerScheme.base = -189

                assertThat(integerScheme.doValidate()).isEqualTo("Base should be in range 2..36.")
            }

            it("fails if the base is 0") {
                integerScheme.base = 0

                assertThat(integerScheme.doValidate()).isEqualTo("Base should be in range 2..36.")
            }

            it("fails if the base is 1") {
                integerScheme.base = 1

                assertThat(integerScheme.doValidate()).isEqualTo("Base should be in range 2..36.")
            }

            it("fails if the base is greater than 36") {
                integerScheme.base = 68

                assertThat(integerScheme.doValidate()).isEqualTo("Base should be in range 2..36.")
            }
        }

        describe("grouping separator") {
            it("fails if the grouping separator contains multiple characters") {
                integerScheme.groupingSeparator = "awc"

                assertThat(integerScheme.doValidate()).isEqualTo("Grouping separator must be at most 1 character.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            integerScheme.minValue = 159
            integerScheme.fixedLengthDecorator.length = 67
            integerScheme.arrayDecorator.minCount = 757

            val copy = integerScheme.deepCopy()
            copy.minValue = 48
            copy.fixedLengthDecorator.length = 61
            copy.arrayDecorator.minCount = 554

            assertThat(integerScheme.minValue).isEqualTo(159)
            assertThat(integerScheme.fixedLengthDecorator.length).isEqualTo(67)
            assertThat(integerScheme.arrayDecorator.minCount).isEqualTo(757)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            integerScheme.minValue = 742
            integerScheme.maxValue = 908
            integerScheme.base = 12
            integerScheme.groupingSeparator = "B"
            integerScheme.customGroupingSeparator = "3"
            integerScheme.capitalization = CapitalizationMode.UPPER
            integerScheme.prefix = "M9d1uey"
            integerScheme.suffix = "m45tL1"
            integerScheme.fixedLengthDecorator.length = 87
            integerScheme.arrayDecorator.minCount = 963

            val newScheme = IntegerScheme()
            newScheme.copyFrom(integerScheme)

            assertThat(newScheme)
                .isEqualTo(integerScheme)
                .isNotSameAs(integerScheme)
            assertThat(newScheme.fixedLengthDecorator)
                .isEqualTo(integerScheme.fixedLengthDecorator)
                .isNotSameAs(integerScheme.fixedLengthDecorator)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(integerScheme.arrayDecorator)
                .isNotSameAs(integerScheme.arrayDecorator)
        }
    }
})
