package com.fwdekker.randomness.word

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [WordScheme].
 */
object WordSchemeTest : Spek({
    lateinit var wordScheme: WordScheme


    beforeEachTest {
        wordScheme = WordScheme(DictionarySettings())
    }


    describe("generateStrings") {
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
                    wordScheme.minLength = minLength
                    wordScheme.maxLength = maxLength
                    wordScheme.enclosure = enclosure

                    val randomString = wordScheme.generateStrings().single()

                    assertThat(randomString)
                        .startsWith(enclosure)
                        .endsWith(enclosure)
                    assertThat(randomString.length)
                        .isGreaterThanOrEqualTo(minLength + 2 * enclosure.length)
                        .isLessThanOrEqualTo(maxLength + 2 * enclosure.length)
                }
            }
        }
    }


    describe("doValidate") {
        val tempFileHelper = TempFileHelper()


        afterGroup {
            tempFileHelper.cleanUp()
        }


        it("passes for the default settings") {
            assertThat(WordScheme(DictionarySettings()).doValidate()).isNull()
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                wordScheme.minLength = -780

                assertThat(wordScheme.doValidate())
                    .isEqualTo("The minimum length should be greater than or equal to 1.")
            }

            it("fails if the length range ends too low to match any words") {
                wordScheme.minLength = 0
                wordScheme.maxLength = 0

                assertThat(wordScheme.doValidate()).isEqualTo(
                    "The shortest word in the selected dictionaries is 1 characters. Set the maximum length to a " +
                        "value less than or equal to 1."
                )
            }

            it("fails if the length range begins too high to match any words") {
                wordScheme.minLength = 1000
                wordScheme.maxLength = 1000

                assertThat(wordScheme.doValidate()).isEqualTo(
                    "The longest word in the selected dictionaries is 15 characters. Set the minimum length to a " +
                        "value less than or equal to 15."
                )
            }
        }

        describe("dictionaries") {
            it("fails if a dictionary of a now-deleted file is given") {
                val dictionaryFile = tempFileHelper.createFile("explore\nworm\ndamp", ".dic").also { it.delete() }
                val dictionary = DictionaryReference(isBundled = false, dictionaryFile.absolutePath)

                wordScheme.activeUserDictionaries = setOf(dictionary)

                assertThat(wordScheme.doValidate()).matches("Dictionary .*\\.dic is invalid: File not found\\.")
            }

            it("fails if no dictionaries are selected") {
                wordScheme.activeUserDictionaries = emptySet()

                assertThat(wordScheme.doValidate()).isEqualTo("Select at least one dictionary.")
            }

            it("fails if one of the dictionaries is invalid") {
                wordScheme.activeUserDictionaries = setOf(DictionaryReference(isBundled = false, "does_not_exist.dic"))

                assertThat(wordScheme.doValidate())
                    .isEqualTo("Dictionary does_not_exist.dic is invalid: File not found.")
            }

            it("fails if one the dictionaries is empty") {
                val dictionaryFile = tempFileHelper.createFile("", ".dic")
                val dictionary = DictionaryReference(isBundled = false, dictionaryFile.absolutePath)

                wordScheme.activeUserDictionaries = setOf(dictionary)

                assertThat(wordScheme.doValidate()).matches("Dictionary .*\\.dic is empty\\.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            wordScheme.minLength = 156

            val copy = wordScheme.deepCopy()
            copy.minLength = 37

            assertThat(wordScheme.minLength).isEqualTo(156)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            wordScheme.minLength = 502
            wordScheme.maxLength = 812
            wordScheme.enclosure = "QJ8S4UrFaa"

            val newScheme = WordScheme(DictionarySettings())
            newScheme.copyFrom(wordScheme)

            assertThat(newScheme).isEqualTo(wordScheme)
            assertThat(newScheme).isNotSameAs(wordScheme)
        }
    }
})
