package com.fwdekker.randomness.word

import com.fwdekker.randomness.Bundle
import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DummyScheme
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [WordScheme].
 */
object WordSchemeTest : Spek({
    lateinit var wordScheme: WordScheme


    beforeEachTest {
        wordScheme = WordScheme()
    }


    describe("generateStrings") {
        describe("words") {
            it("returns one of the selected words") {
                wordScheme.words = listOf("pad", "obedient", "faint", "desert", "church")
                wordScheme.quotation = ""

                assertThat(wordScheme.words).contains(wordScheme.generateStrings().single())
            }

            it("returns different words") {
                wordScheme.words = List(10_000) { it.toString() }
                wordScheme.quotation = ""

                assertThat(wordScheme.generateStrings(2).toSet()).hasSize(2) // Chance of 1/10000 = 0.01% to fail
            }
        }

        describe("quotation") {
            it("adds no quotations if the quotations are an empty string") {
                wordScheme.words = listOf("show")
                wordScheme.quotation = ""

                assertThat(wordScheme.generateStrings().single()).isEqualTo("show")
            }

            it("repeats the first character of the quotations on both ends") {
                wordScheme.words = listOf("country")
                wordScheme.quotation = "L"

                assertThat(wordScheme.generateStrings().single()).isEqualTo("LcountryL")
            }

            it("surrounds the output with the respective characters of the quotation string") {
                wordScheme.words = listOf("argue")
                wordScheme.quotation = "pn"

                assertThat(wordScheme.generateStrings().single()).isEqualTo("parguen")
            }
        }

        describe("capitalization") {
            it("applies the given capitalization") {
                wordScheme.words = listOf("hold")
                wordScheme.quotation = ""
                wordScheme.capitalization = CapitalizationMode.UPPER

                assertThat(wordScheme.generateStrings().single()).isEqualTo("HOLD")
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(wordScheme.doValidate()).isNull()
        }

        it("fails if the decorator is invalid") {
            wordScheme.arrayDecorator.minCount = -88

            assertThat(wordScheme.doValidate()).isNotNull()
        }

        it("fails if the custom quotation has more than two characters") {
            wordScheme.customQuotation = "3D7F"

            assertThat(wordScheme.doValidate()).isEqualTo(Bundle("word.error.quotation_length"))
        }

        it("fails if the list of words is empty") {
            wordScheme.words = emptyList()

            assertThat(wordScheme.doValidate()).isEqualTo(Bundle("word.error.empty_word_list"))
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            wordScheme.words = listOf("soul", "suspect", "due")
            wordScheme.arrayDecorator.minCount = 333

            val copy = wordScheme.deepCopy()
            copy.words = listOf("lonely", "travel", "alive")
            copy.arrayDecorator.minCount = 531

            assertThat(wordScheme.words).containsExactly("soul", "suspect", "due")
            assertThat(wordScheme.arrayDecorator.minCount).isEqualTo(333)
        }

        it("creates an independent list of words") {
            val list = mutableListOf("habit", "new")
            wordScheme.words = list
            val copy = wordScheme.deepCopy()

            list += "citizen"

            assertThat(copy.words).doesNotContain("citizen")
        }
    }

    describe("copyFrom") {
        it("cannot copy from a different type") {
            assertThatThrownBy { wordScheme.copyFrom(DummyScheme()) }.isNotNull()
        }

        it("copies state from another instance") {
            wordScheme.words = listOf("strap", "tip", "tray")
            wordScheme.quotation = "'"
            wordScheme.customQuotation = "Ae"
            wordScheme.capitalization = CapitalizationMode.LOWER
            wordScheme.arrayDecorator.minCount = 513

            val newScheme = WordScheme()
            newScheme.copyFrom(wordScheme)

            assertThat(newScheme)
                .isEqualTo(wordScheme)
                .isNotSameAs(wordScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(wordScheme.arrayDecorator)
                .isNotSameAs(wordScheme.arrayDecorator)
        }
    }
})
