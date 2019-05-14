package com.fwdekker.randomness.integer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Unit tests for the symbols used in [IntegerInsertAction].
 */
class IntegerInsertActionSymbolTest {
    companion object {
        @JvmStatic
        fun provider() =
            listOf(
                arrayOf(95713, "", "95713"),
                arrayOf(163583, ".", "163.583"),
                arrayOf(351426, ",", "351,426")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(value: Long, groupingSeparator: String, expectedString: String) {
        val integerSettings = IntegerSettings()
        integerSettings.minValue = value
        integerSettings.maxValue = value
        integerSettings.groupingSeparator = groupingSeparator

        val insertRandomInteger = IntegerInsertAction(integerSettings)
        val randomString = insertRandomInteger.generateString()

        assertThat(randomString).isEqualTo(expectedString)
    }
}
