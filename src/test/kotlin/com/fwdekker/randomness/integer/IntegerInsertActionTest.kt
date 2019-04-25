package com.fwdekker.randomness.integer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [IntegerInsertAction].
 */
class IntegerInsertActionTest {
    companion object {
        @JvmStatic
        fun provider() =
            listOf(
                arrayOf(0, 0, "0"),
                arrayOf(1, 1, "1"),
                arrayOf(-5, -5, "-5"),
                arrayOf(488, 488, "488"),
                arrayOf(-876, -876, "-876")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(minValue: Long, maxValue: Long, expectedString: String) {
        val integerSettings = IntegerSettings()
        integerSettings.minValue = minValue
        integerSettings.maxValue = maxValue

        val insertRandomInteger = IntegerInsertAction(integerSettings)
        val randomString = insertRandomInteger.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }
}
