package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


/**
 * Parameterized unit tests for [WordInsertAction].
 */
class WordInsertActionParamTest {
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
}


/**
 * Unit tests for [WordInsertAction].
 */
object WordInsertActionTest : Spek({
    val tempFileHelper = TempFileHelper()


    afterGroup {
        tempFileHelper.cleanUp()
    }


    describe("error handling") {
        it("throws an exception if there are no active dictionaries") {
            val wordSettings = WordSettings()
            wordSettings.activeBundledDictionaries = emptySet()

            val insertRandomWord = WordInsertAction(wordSettings)

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("There are no active dictionaries.")
                .hasNoCause()
        }

        it("throws an exception if an active dictionary no longer exists") {
            val dictionaryFile = tempFileHelper.createFile("", ".dic")
            val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)
            dictionaryFile.delete()

            val wordSettings = WordSettings()
            wordSettings.activeBundledDictionaries = emptySet()
            wordSettings.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction(wordSettings)

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Failed to read user dictionary into memory.")
                .hasCauseInstanceOf(InvalidDictionaryException::class.java)
        }

        it("throws an exception if all active dictionaries are empty") {
            val dictionaryFile = tempFileHelper.createFile("", ".dic")
            val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

            val wordSettings = WordSettings()
            wordSettings.activeBundledDictionaries = emptySet()
            wordSettings.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction(wordSettings)

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("All active dictionaries are empty.")
                .hasNoCause()
        }

        it("throws an exception if there are no words in the configured range") {
            val dictionaryFile = tempFileHelper.createFile("a", ".dic")
            val dictionary = UserDictionary.cache.get(dictionaryFile.absolutePath, false)

            val wordSettings = WordSettings()
            wordSettings.minLength = 2
            wordSettings.activeBundledDictionaries = emptySet()
            wordSettings.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction(wordSettings)

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("There are no words within the configured length range.")
                .hasNoCause()
        }
    }
})
