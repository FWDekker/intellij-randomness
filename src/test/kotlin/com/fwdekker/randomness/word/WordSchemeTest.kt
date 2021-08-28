package com.fwdekker.randomness.word

import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.TempFileHelper
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
                Triple(1, 1, ""),
                Triple(12, 12, ""),
                Triple(3, 15, "\""),
                Triple(3, 13, "`"),
                Triple(7, 9, "delim")
            ).forEach { (minLength, maxLength, enclosure) ->
                it("generates a formatted word between $minLength and $maxLength characters") {
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

    describe("setSettingsState") {
        it("overwrites the constructor's symbol set settings") {
            val newSettings = SettingsState(dictionarySettings = DictionarySettings())

            wordScheme.setSettingsState(newSettings)

            assertThat(wordScheme.dictionarySettings).isSameAs(newSettings.dictionarySettings)
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

                assertThat(wordScheme.doValidate()).isEqualTo("Minimum length should not be smaller than 1.")
            }

            it("fails if the minimum length is larger than the maximum length") {
                wordScheme.minLength = 198
                wordScheme.maxLength = 98

                assertThat(wordScheme.doValidate())
                    .isEqualTo("Minimum length should not be larger than maximum length.")
            }

            it("fails if the length range ends too low to match any words") {
                val dictionaryFile = tempFileHelper.createFile("save", ".dic")
                val dictionary = DictionaryReference(isBundled = false, dictionaryFile.absolutePath)

                wordScheme.activeBundledDictionaries = emptySet()
                wordScheme.activeUserDictionaries = setOf(dictionary)
                wordScheme.minLength = 1
                wordScheme.maxLength = 1

                assertThat(wordScheme.doValidate()).isEqualTo(
                    "The shortest word in the selected dictionaries is 4 characters. Set the maximum length to a " +
                        "value less than or equal to 4."
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
            it("fails if the dictionary settings are invalid") {
                val dictionaryFile = tempFileHelper.createFile("heavenly\npet\n", ".dic").also { it.delete() }

                wordScheme.dictionarySettings.userDictionaryFiles = mutableSetOf(dictionaryFile.absolutePath)

                assertThat(wordScheme.doValidate()).isNotNull()
            }

            it("fails if no dictionaries are selected") {
                wordScheme.activeBundledDictionaries = emptySet()
                wordScheme.activeUserDictionaries = emptySet()

                assertThat(wordScheme.doValidate()).isEqualTo("Activate at least one dictionary.")
            }
        }

        describe("decorator") {
            it("fails if the decorator is invalid") {
                wordScheme.decorator.count = -88

                assertThat(wordScheme.doValidate()).isNotNull()
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            wordScheme.minLength = 156
            wordScheme.decorator.count = 333

            val copy = wordScheme.deepCopy()
            copy.minLength = 37
            copy.decorator.count = 531

            assertThat(wordScheme.minLength).isEqualTo(156)
            assertThat(wordScheme.decorator.count).isEqualTo(333)
        }

        it("retains the reference to the dictionary settings") {
            assertThat(wordScheme.deepCopy().dictionarySettings).isSameAs(wordScheme.dictionarySettings)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            wordScheme.minLength = 502
            wordScheme.maxLength = 812
            wordScheme.enclosure = "QJ8S4UrFaa"
            wordScheme.decorator.count = 513

            val newScheme = WordScheme(DictionarySettings())
            newScheme.copyFrom(wordScheme)

            assertThat(newScheme)
                .isEqualTo(wordScheme)
                .isNotSameAs(wordScheme)
            assertThat(newScheme.decorator)
                .isEqualTo(wordScheme.decorator)
                .isNotSameAs(wordScheme.decorator)
        }

        it("retains the reference to the symbol set settings") {
            val newSettings = DictionarySettings()

            wordScheme.copyFrom(WordScheme(newSettings))

            assertThat(wordScheme.dictionarySettings).isSameAs(newSettings)
        }
    }
})