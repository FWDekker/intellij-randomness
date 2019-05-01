package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [WordInsertAction].
 */
class WordInsertActionTest {
    companion object {
        @JvmStatic
        fun provider() =
            listOf(
                arrayOf(0, 1, ""),
                arrayOf(1, 1, ""),
                arrayOf(12, 12, ""),
                arrayOf(3, 15, "\""),
                arrayOf(3, 13, "`"),
                arrayOf(7, 9, "delim")
            )
    }


    @ParameterizedTest
    @MethodSource("provider")
    fun testValue(minLength: Int, maxLength: Int, enclosure: String) {
        val wordSettings = WordSettings()
        wordSettings.minLength = minLength
        wordSettings.maxLength = maxLength
        wordSettings.enclosure = enclosure

        val insertRandomWord = WordInsertAction(wordSettings)
        val randomString = insertRandomWord.generateString()

        assertThat(randomString)
            .startsWith(enclosure)
            .endsWith(enclosure)
        assertThat(randomString.length)
            .isGreaterThanOrEqualTo(minLength + 2 * enclosure.length)
            .isLessThanOrEqualTo(maxLength + 2 * enclosure.length)
    }

    @Test
    fun testNoValidWords() {
        val wordSettings = WordSettings()
        wordSettings.activeBundledDictionaries = emptySet()

        val insertRandomWord = WordInsertAction(wordSettings)

        assertThatThrownBy { insertRandomWord.generateString() }
            .isInstanceOf(DataGenerationException::class.java)
            .hasMessage("There are no words compatible with the current settings.")
            .hasNoCause()
    }
}
