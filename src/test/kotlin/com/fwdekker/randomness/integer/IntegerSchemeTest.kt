package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [IntegerScheme].
 */
object IntegerSchemeTest : Spek({
    lateinit var integerScheme: IntegerScheme


    beforeEachTest {
        integerScheme = IntegerScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            integerScheme.minValue = 65L
            integerScheme.maxValue = 24L

            assertThatThrownBy { integerScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        describe("value range") {
            mapOf(
                Pair(0L, 0L) to "0",
                Pair(1L, 1L) to "1",
                Pair(-5L, -5L) to "-5",
                Pair(488L, 488L) to "488",
                Pair(-876L, -876L) to "-876",
                Pair(Long.MIN_VALUE, Long.MIN_VALUE) to Long.MIN_VALUE.toString(),
                Pair(Long.MAX_VALUE, Long.MAX_VALUE) to Long.MAX_VALUE.toString(),
            ).forEach { (minValue, maxValue), expectedString ->
                it("generates $expectedString") {
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
            mapOf(
                Triple(33_360L, 10, ".") to "33.360",
                Triple(48_345L, 10, ".") to "48.345",
                Triple(48_345L, 11, ".") to "33360"
            ).forEach { (value, base, groupingSeparator), expectedString ->
                it("generates $expectedString") {
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.base = base
                    integerScheme.groupingSeparator = groupingSeparator

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("separator") {
            mapOf(
                Pair(95_713L, "") to "95713",
                Pair(163_583L, ".") to "163.583",
                Pair(351_426L, ",") to "351,426"
            ).forEach { (value, groupingSeparator), expectedString ->
                it("generates $expectedString") {
                    integerScheme.minValue = value
                    integerScheme.maxValue = value
                    integerScheme.groupingSeparator = groupingSeparator

                    assertThat(integerScheme.generateStrings()).containsExactly(expectedString)
                }
            }
        }

        describe("capitalization") {
            data class Param(val value: Long, val base: Int, val prefix: String, val capitalization: CapitalizationMode)

            mapOf(
                Param(624L, 10, "", CapitalizationMode.UPPER) to "624",
                Param(254L, 16, "", CapitalizationMode.UPPER) to "FE",
                Param(254L, 16, "0x", CapitalizationMode.FIRST_LETTER) to "0xFe",
            ).forEach { (value, base, prefix, capitalization), expectedString ->
                it("generates $expectedString") {
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
            mapOf(
                Triple(920L, "", "") to "920",
                Triple(553L, "before", "after") to "before553after",
                Triple(948L, "0x", "") to "0x948",
                Triple(502L, "", "L") to "502L"
            ).forEach { (value, prefix, suffix), expectedString ->
                it("generates $expectedString") {
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

        describe("decorator") {
            it("fails if the decorator is invalid") {
                integerScheme.arrayDecorator.minCount = -584

                assertThat(integerScheme.doValidate()).isNotNull()
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            integerScheme.minValue = 159
            integerScheme.arrayDecorator.maxCount = 757

            val copy = integerScheme.deepCopy()
            copy.minValue = 48
            copy.arrayDecorator.maxCount = 554

            assertThat(integerScheme.minValue).isEqualTo(159)
            assertThat(integerScheme.arrayDecorator.maxCount).isEqualTo(757)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            integerScheme.minValue = 742
            integerScheme.maxValue = 908
            integerScheme.base = 12
            integerScheme.arrayDecorator.maxCount = 963

            val newScheme = IntegerScheme()
            newScheme.copyFrom(integerScheme)

            assertThat(newScheme)
                .isEqualTo(integerScheme)
                .isNotSameAs(integerScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(integerScheme.arrayDecorator)
                .isNotSameAs(integerScheme.arrayDecorator)
        }
    }
})
