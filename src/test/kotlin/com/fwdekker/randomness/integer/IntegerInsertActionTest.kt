package com.fwdekker.randomness.integer

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupActionTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [IntegerInsertAction].
 */
class IntegerInsertActionTest : Spek({
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
                val integerScheme = IntegerScheme(minValue = minValue, maxValue = maxValue)

                val insertRandomInteger = IntegerInsertAction(integerScheme)
                val randomString = insertRandomInteger.generateString()

                assertThat(randomString).isEqualTo(expectedString)
            }
        }

        it("throws an exception if the minimum is larger than the maximum") {
            val action = IntegerInsertAction(IntegerScheme(minValue = 65, maxValue = 24))
            Assertions.assertThatThrownBy { action.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Minimum value is larger than maximum value.")
        }

        it("generates a random value at maximum range size") {
            val integerScheme = IntegerScheme(minValue = Long.MIN_VALUE, maxValue = Long.MAX_VALUE)

            val insertRandomInteger = IntegerInsertAction(integerScheme)
            val randomString = insertRandomInteger.generateString()

            // Passes with extremely high probability (p = 1 - (2/(2^64))
            assertThat(randomString)
                .isNotEqualTo(Long.MIN_VALUE.toString())
                .isNotEqualTo(Long.MAX_VALUE.toString())
        }
    }

    describe("base") {
        mapOf(
            Triple(33360L, 10, ".") to "33.360",
            Triple(48345L, 10, ".") to "48.345",
            Triple(48345L, 11, ".") to "33360"
        ).forEach { (value, base, groupingSeparator), expectedString ->
            it("generates $expectedString") {
                val integerScheme = IntegerScheme(
                    minValue = value,
                    maxValue = value,
                    base = base,
                    groupingSeparator = groupingSeparator
                )

                val insertRandomInteger = IntegerInsertAction(integerScheme)
                val randomString = insertRandomInteger.generateString()

                assertThat(randomString).isEqualTo(expectedString)
            }
        }
    }

    describe("separator") {
        mapOf(
            Pair(95713L, "") to "95713",
            Pair(163583L, ".") to "163.583",
            Pair(351426L, ",") to "351,426"
        ).forEach { (value, groupingSeparator), expectedString ->
            it("generates $expectedString") {
                val integerScheme = IntegerScheme(
                    minValue = value,
                    maxValue = value,
                    groupingSeparator = groupingSeparator
                )

                val insertRandomInteger = IntegerInsertAction(integerScheme)
                val randomString = insertRandomInteger.generateString()

                assertThat(randomString).isEqualTo(expectedString)
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
                val integerScheme = IntegerScheme(
                    minValue = value,
                    maxValue = value,
                    base = base,
                    prefix = prefix,
                    capitalization = capitalization
                )

                val insertRandomInteger = IntegerInsertAction(integerScheme)
                val randomString = insertRandomInteger.generateString()

                assertThat(randomString).isEqualTo(expectedString)
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
                val integerScheme = IntegerScheme(
                    minValue = value,
                    maxValue = value,
                    prefix = prefix,
                    suffix = suffix
                )

                val insertRandomInteger = IntegerInsertAction(integerScheme)
                val randomString = insertRandomInteger.generateString()

                assertThat(randomString).isEqualTo(expectedString)
            }
        }
    }
})


/**
 * Unit tests for [IntegerGroupAction].
 */
class IntegerGroupActionTest : DataGroupActionTest({ IntegerGroupAction() })
