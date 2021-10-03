package com.fwdekker.randomness.word

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.TempFileHelper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [WordScheme].
 */
object WordSchemeTest : Spek({
    lateinit var tempFileHelper: TempFileHelper
    lateinit var dictionarySettings: DictionarySettings
    lateinit var wordScheme: WordScheme


    beforeGroup {
        tempFileHelper = TempFileHelper()
    }

    beforeEachTest {
        dictionarySettings = DictionarySettings()
        wordScheme = WordScheme()
        wordScheme.dictionarySettings += dictionarySettings
    }

    afterGroup {
        tempFileHelper.cleanUp()
    }


    describe("generateStrings") {
        describe("pattern") {
            listOf(
                Triple(1, 1, ""),
                Triple(12, 12, ""),
                Triple(3, 15, "\""),
                Triple(3, 13, "`"),
                Triple(7, 9, "d")
            ).forEach { (minLength, maxLength, quotation) ->
                it("generates a formatted word between $minLength and $maxLength characters") {
                    wordScheme.minLength = minLength
                    wordScheme.maxLength = maxLength
                    wordScheme.quotation = quotation

                    val randomString = wordScheme.generateStrings().single()

                    assertThat(randomString)
                        .startsWith(quotation)
                        .endsWith(quotation)
                    assertThat(randomString.length)
                        .isGreaterThanOrEqualTo(minLength + 2 * quotation.length)
                        .isLessThanOrEqualTo(maxLength + 2 * quotation.length)
                }
            }
        }

        describe("quotation") {
            it("adds no quotations if the quotations are an empty string") {
                val dictionary = tempFileHelper.createFile("show")

                wordScheme.quotation = ""
                wordScheme.activeDictionaries = setOf(UserDictionary(dictionary.absolutePath))

                assertThat(wordScheme.generateStrings().single()).isEqualTo("show")
            }

            it("repeats the first character of the quotations on both ends") {
                val dictionary = tempFileHelper.createFile("country")

                wordScheme.quotation = "L"
                wordScheme.activeDictionaries = setOf(UserDictionary(dictionary.absolutePath))

                assertThat(wordScheme.generateStrings().single()).isEqualTo("LcountryL")
            }

            it("surrounds the output with the respective characters of the quotation string") {
                val dictionary = tempFileHelper.createFile("argue")

                wordScheme.quotation = "pn"
                wordScheme.activeDictionaries = setOf(UserDictionary(dictionary.absolutePath))

                assertThat(wordScheme.generateStrings().single()).isEqualTo("parguen")
            }
        }
    }

    describe("setSettingsState") {
        it("overwrites the default symbol set settings") {
            val newSettings = DictionarySettings()

            wordScheme.setSettingsState(SettingsState(dictionarySettings = newSettings))

            assertThat(+wordScheme.dictionarySettings).isSameAs(newSettings)
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(wordScheme.doValidate()).isNull()
        }

        it("fails if the decorator is invalid") {
            wordScheme.arrayDecorator.count = -88

            assertThat(wordScheme.doValidate()).isNotNull()
        }

        it("fails if the custom quotation has more than two characters") {
            wordScheme.customQuotation = "3D7F"

            assertThat(wordScheme.doValidate()).isEqualTo("Quotation must be at most 2 characters.")
        }

        describe("length range") {
            it("fails if the minimum length is negative") {
                wordScheme.minLength = -780

                assertThat(wordScheme.doValidate()).isEqualTo("Minimum length should be at least 1.")
            }

            it("fails if the minimum length is larger than the maximum length") {
                wordScheme.minLength = 198
                wordScheme.maxLength = 98

                assertThat(wordScheme.doValidate())
                    .isEqualTo("Minimum length should be less than or equal to maximum length.")
            }

            it("fails if the length range ends too low to match any words") {
                val file = tempFileHelper.createFile("save", ".dic")
                wordScheme.activeDictionaries = setOf(UserDictionary(file.absolutePath))

                wordScheme.minLength = 1
                wordScheme.maxLength = 1

                assertThat(wordScheme.doValidate()).isEqualTo(
                    "Shortest word in selected dictionaries is 4 characters. Maximum length should be greater than " +
                        "or equal to 4."
                )
            }

            it("fails if the length range begins too high to match any words") {
                wordScheme.minLength = 1000
                wordScheme.maxLength = 1000

                assertThat(wordScheme.doValidate()).isEqualTo(
                    "Longest word in selected dictionaries is 15 characters. Minimum length should be less than or " +
                        "equal to 15."
                )
            }
        }

        describe("dictionaries") {
            it("fails if the dictionary settings are invalid") {
                val file = tempFileHelper.createFile("heavenly\npet\n", ".dic").also { it.delete() }

                dictionarySettings.dictionaries = listOf(UserDictionary(file.absolutePath))

                assertThat(wordScheme.doValidate()).isNotNull()
            }

            it("fails if no dictionaries are selected") {
                wordScheme.activeDictionaries = emptySet()

                assertThat(wordScheme.doValidate()).isEqualTo("Activate at least one dictionary.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            wordScheme.minLength = 156
            wordScheme.arrayDecorator.count = 333

            val copy = wordScheme.deepCopy()
            copy.minLength = 37
            copy.arrayDecorator.count = 531

            assertThat(wordScheme.minLength).isEqualTo(156)
            assertThat(wordScheme.arrayDecorator.count).isEqualTo(333)
        }

        it("creates an independent copy of the dictionary settings box") {
            val copy = wordScheme.deepCopy(retainUuid = true)
            copy.dictionarySettings += DictionarySettings()

            assertThat(+copy.dictionarySettings).isNotSameAs(+wordScheme.dictionarySettings)
        }

        it("creates an independent copy of the dictionary settings") {
            (+wordScheme.dictionarySettings).dictionaries = listOf(UserDictionary("classify.dic"))

            val copy = wordScheme.deepCopy()
            (+copy.dictionarySettings).dictionaries = listOf(UserDictionary("decay.dic"))

            assertThat((+wordScheme.dictionarySettings).dictionaries).containsExactly(UserDictionary("classify.dic"))
        }
    }

    describe("copyFrom") {
        it("cannot copy from a different type") {
            assertThatThrownBy { wordScheme.copyFrom(DummyScheme()) }.isNotNull()
        }

        it("copies state from another instance") {
            wordScheme.minLength = 502
            wordScheme.maxLength = 812
            wordScheme.quotation = "xs"
            wordScheme.customQuotation = "Ae"
            wordScheme.arrayDecorator.count = 513

            val newScheme = WordScheme()
            newScheme.dictionarySettings += DictionarySettings()
            newScheme.copyFrom(wordScheme)

            assertThat(newScheme)
                .isEqualTo(wordScheme)
                .isNotSameAs(wordScheme)
            assertThat(+newScheme.dictionarySettings)
                .isEqualTo(+wordScheme.dictionarySettings)
                .isNotSameAs(+wordScheme.dictionarySettings)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(wordScheme.arrayDecorator)
                .isNotSameAs(wordScheme.arrayDecorator)
        }

        it("does not change the target's reference to the dictionary settings") {
            wordScheme.copyFrom(WordScheme().also { it.dictionarySettings += DictionarySettings() })

            assertThat(+wordScheme.dictionarySettings).isSameAs(dictionarySettings)
        }

        it("writes a deep copy of the given scheme's dictionary settings into the target") {
            val otherSettings = DictionarySettings(listOf(UserDictionary("complain.dic")))
            val otherScheme = WordScheme()
            otherScheme.dictionarySettings += otherSettings

            wordScheme.copyFrom(otherScheme)
            (+otherScheme.dictionarySettings).dictionaries = listOf(UserDictionary("spit.dic"))

            assertThat((+wordScheme.dictionarySettings).dictionaries).containsExactly(UserDictionary("complain.dic"))
        }
    }
})
