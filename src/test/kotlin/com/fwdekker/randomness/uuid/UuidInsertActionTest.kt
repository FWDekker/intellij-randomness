package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [UuidInsertAction].
 */
class UuidInsertActionTest {
    companion object {
        @JvmStatic
        private fun provider() = listOf(
            arrayOf(1, "", CapitalizationMode.LOWER, true),
            arrayOf(4, "'", CapitalizationMode.LOWER, true),
            arrayOf(1, "Eglzfpf5", CapitalizationMode.LOWER, true),
            arrayOf(4, "", CapitalizationMode.UPPER, true),
            arrayOf(1, "'", CapitalizationMode.UPPER, false)
        )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testEnclosure(version: Int, enclosure: String, capitalization: CapitalizationMode, addDashes: Boolean) {
        val uuidScheme = UuidScheme(
            version = version,
            enclosure = enclosure,
            capitalization = capitalization,
            addDashes = addDashes
        )

        val insertRandomUuid = UuidInsertAction(uuidScheme)
        val generatedString = insertRandomUuid.generateString()

        val alphabet = capitalization.transform("0-9a-fA-F")
        val dash = if (addDashes) "-" else ""

        assertThat(
            Regex("" +
                "^$enclosure" +
                "[$alphabet]{8}$dash[$alphabet]{4}$dash[$alphabet]{4}$dash[$alphabet]{4}$dash[$alphabet]{12}" +
                "$enclosure$"
            ).matches(generatedString)
        ).isTrue()
    }

    @Test
    fun testInvalidVersion() {
        assertThatThrownBy { UuidInsertAction(UuidScheme(version = 9)).generateString() }
            .isInstanceOf(DataGenerationException::class.java)
            .hasMessage("Unknown UUID version `9`.")
    }
}
