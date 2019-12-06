package com.fwdekker.randomness.uuid

import com.fwdekker.randomness.CapitalizationMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [UuidInsertAction].
 */
class UuidInsertActionTest {
    companion object {
        @JvmStatic
        private fun provider() = listOf(
            arrayOf("", CapitalizationMode.LOWER, true),
            arrayOf("'", CapitalizationMode.LOWER, true),
            arrayOf("Eglzfpf5", CapitalizationMode.LOWER, true),
            arrayOf("", CapitalizationMode.UPPER, true),
            arrayOf("'", CapitalizationMode.UPPER, false)
        )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testEnclosure(enclosure: String, capitalizationMode: CapitalizationMode, addDashes: Boolean) {
        val insertRandomUuid = UuidInsertAction(UuidSettings(enclosure, capitalizationMode, addDashes))
        val generatedString = insertRandomUuid.generateString()

        val alphabet = capitalizationMode.transform("0-9a-fA-F")
        val dash = if (addDashes) "-" else ""

        assertThat(
            Regex("" +
                "^$enclosure" +
                "[$alphabet]{8}$dash[$alphabet]{4}$dash[$alphabet]{4}$dash[$alphabet]{4}$dash[$alphabet]{12}" +
                "$enclosure$"
            ).matches(generatedString)
        ).isTrue()
    }
}
