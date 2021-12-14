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
        describe("quotation") {
            it("adds no quotations if the quotations are an empty string") {
                wordScheme.quotation = ""
                wordScheme.words = listOf("show")

                assertThat(wordScheme.generateStrings().single()).isEqualTo("show")
            }

            it("repeats the first character of the quotations on both ends") {
                wordScheme.quotation = "L"
                wordScheme.words = listOf("country")

                assertThat(wordScheme.generateStrings().single()).isEqualTo("LcountryL")
            }

            it("surrounds the output with the respective characters of the quotation string") {
                wordScheme.quotation = "pn"
                wordScheme.words = listOf("argue")

                assertThat(wordScheme.generateStrings().single()).isEqualTo("parguen")
            }
        }

        describe("capitalization") {
            it("applies the given capitalization") {
                wordScheme.quotation = ""
                wordScheme.capitalization = CapitalizationMode.UPPER
                wordScheme.words = listOf("hold")

                assertThat(wordScheme.generateStrings().single()).isEqualTo("HOLD")
            }
        }

        describe("words") {
            it("returns one of the selected words") {
                wordScheme.quotation = ""
                wordScheme.words = listOf("pad", "obedient", "faint", "desert", "church")

                assertThat(wordScheme.words).contains(wordScheme.generateStrings().single())
            }

            it("returns different words") {
                wordScheme.quotation = ""
                wordScheme.words = List(10_000) { it.toString() }

                assertThat(wordScheme.generateStrings(2).toSet()).hasSize(2) // Chance of 1/10000 = 0.01% to fail
            }
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
            wordScheme.arrayDecorator.count = 333

            val copy = wordScheme.deepCopy()
            copy.words = listOf("lonely", "travel", "alive")
            copy.arrayDecorator.count = 531

            assertThat(wordScheme.words).containsExactly("soul", "suspect", "due")
            assertThat(wordScheme.arrayDecorator.count).isEqualTo(333)
        }
    }

    describe("copyFrom") {
        it("cannot copy from a different type") {
            assertThatThrownBy { wordScheme.copyFrom(DummyScheme()) }.isNotNull()
        }

        it("copies state from another instance") {
            wordScheme.quotation = "xs"
            wordScheme.customQuotation = "Ae"
            wordScheme.capitalization = CapitalizationMode.LOWER
            wordScheme.words = listOf("strap", "tip", "tray")
            wordScheme.arrayDecorator.count = 513

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
