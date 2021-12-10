package com.fwdekker.randomness.word

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DataGroupActionTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [WordInsertAction].
 */
class WordInsertActionParamTest : Spek({
    describe("pattern") {
        listOf(
            Triple(0, 1, ""),
            Triple(1, 1, ""),
            Triple(12, 12, ""),
            Triple(3, 15, "\""),
            Triple(3, 13, "`"),
            Triple(7, 9, "delim")
        ).forEach { (minLength, maxLength, enclosure) ->
            it("generates a formatted word") {
                val wordScheme = WordScheme(minLength = minLength, maxLength = maxLength, enclosure = enclosure)

                val insertRandomWord = WordInsertAction { wordScheme }
                val randomString = insertRandomWord.generateString()

                assertThat(randomString)
                    .startsWith(enclosure)
                    .endsWith(enclosure)
                assertThat(randomString.length)
                    .isGreaterThanOrEqualTo(minLength + 2 * enclosure.length)
                    .isLessThanOrEqualTo(maxLength + 2 * enclosure.length)
            }
        }
    }

    describe("error handling") {
        val tempFileHelper = TempFileHelper()


        afterGroup {
            tempFileHelper.cleanUp()
        }


        it("throws an exception if there are no active dictionaries") {
            val wordScheme = WordScheme()
            wordScheme.activeBundledDictionaries = emptySet()

            val insertRandomWord = WordInsertAction { wordScheme }

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("There are no active dictionaries.")
                .hasNoCause()
        }

        it("throws an exception if an active dictionary no longer exists") {
            val dictionaryFile = tempFileHelper.createFile("", ".dic")
            val dictionary = DictionaryReference(false, dictionaryFile.absolutePath)
            dictionaryFile.delete()

            val wordScheme = WordScheme()
            wordScheme.activeBundledDictionaries = emptySet()
            wordScheme.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction { wordScheme }

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("File not found.")
                .hasCauseInstanceOf(InvalidDictionaryException::class.java)
        }

        it("throws an exception if all active dictionaries are empty") {
            val dictionaryFile = tempFileHelper.createFile("", ".dic")
            val dictionary = DictionaryReference(false, dictionaryFile.absolutePath)

            val wordScheme = WordScheme()
            wordScheme.activeBundledDictionaries = emptySet()
            wordScheme.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction { wordScheme }

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("All active dictionaries are empty.")
                .hasNoCause()
        }

        it("throws an exception if there are no words in the configured range") {
            val dictionaryFile = tempFileHelper.createFile("a", ".dic")
            val dictionary = DictionaryReference(false, dictionaryFile.absolutePath)

            val wordScheme = WordScheme()
            wordScheme.minLength = 2
            wordScheme.activeBundledDictionaries = emptySet()
            wordScheme.activeUserDictionaries = setOf(dictionary)

            val insertRandomWord = WordInsertAction { wordScheme }

            assertThatThrownBy { insertRandomWord.generateString() }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("There are no words within the configured length range.")
                .hasNoCause()
        }
    }
})


/**
 * Unit tests for [WordGroupAction].
 */
class WordGroupActionTest : DataGroupActionTest({ WordGroupAction() })
