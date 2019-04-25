package com.fwdekker.randomness.uuid

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID


/**
 * Parameterized unit tests for [UuidInsertAction].
 */
class UuidInsertActionTest {
    companion object {
        @JvmStatic
        private fun provider() = listOf(
            arrayOf<Any>(""), arrayOf<Any>("'"), arrayOf<Any>("Eglzfpf5")
        )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(enclosure: String) {
        val uuidSettings = UuidSettings()
        uuidSettings.enclosure = enclosure

        val insertRandomUuid = UuidInsertAction(uuidSettings)
        val generatedString = insertRandomUuid.generateString()

        assertThat(generatedString)
            .startsWith(enclosure)
            .endsWith(enclosure)

        val generatedUuid = generatedString
            .replace("^$enclosure".toRegex(), "")
            .replace("$enclosure$".toRegex(), "")
        assertThatCode { UUID.fromString(generatedUuid) }.doesNotThrowAnyException()
    }
}
