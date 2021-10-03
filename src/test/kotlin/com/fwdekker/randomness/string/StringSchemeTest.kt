package com.fwdekker.randomness.string

import com.fwdekker.randomness.CapitalizationMode
import com.fwdekker.randomness.DataGenerationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [StringScheme].
 */
object StringSchemeTest : Spek({
    lateinit var stringScheme: StringScheme


    beforeEachTest {
        stringScheme = StringScheme()
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            stringScheme.pattern = "[a-Z"

            assertThatThrownBy { stringScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns the non-regex pattern") {
            stringScheme.pattern = "mercy"

            assertThat(stringScheme.generateStrings()).containsExactly("mercy")
        }

        it("returns the capitalized non-regex pattern") {
            stringScheme.pattern = "mean"
            stringScheme.capitalization = CapitalizationMode.UPPER

            assertThat(stringScheme.generateStrings()).containsExactly("MEAN")
        }

        it("returns a reverse-regexed string") {
            stringScheme.pattern = "[a-z]{3} warn [A-Z]{2,3}"

            repeat(256) {
                assertThat(stringScheme.generateStrings().single()).matches(stringScheme.pattern)
            }
        }

        it("returns a reverse-regexed string without look-alike characters") {
            stringScheme.pattern = "[a-z]{4}"

            repeat(256) {
                assertThat(stringScheme.generateStrings().single()).matches(stringScheme.pattern)
            }
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(stringScheme.doValidate()).isNull()
        }

        it("fails if the pattern is invalid") {
            stringScheme.pattern = "{9"

            assertThat(stringScheme.doValidate()).isEqualTo("Cannot repeat nothing at\n'{9'\n ^")
        }

        it("fails if the decorator is invalid") {
            stringScheme.arrayDecorator.count = -985

            assertThat(stringScheme.doValidate()).isNotNull()
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            stringScheme.pattern = "compose"
            stringScheme.arrayDecorator.count = 943

            val copy = stringScheme.deepCopy()
            copy.pattern = "tidy"
            copy.arrayDecorator.count = 173

            assertThat(stringScheme.pattern).isEqualTo("compose")
            assertThat(stringScheme.arrayDecorator.count).isEqualTo(943)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            stringScheme.pattern = "trust"
            stringScheme.capitalization = CapitalizationMode.RANDOM
            stringScheme.removeLookAlikeSymbols = true
            stringScheme.arrayDecorator.count = 249

            val newScheme = StringScheme()
            newScheme.copyFrom(stringScheme)

            assertThat(newScheme)
                .isEqualTo(stringScheme)
                .isNotSameAs(stringScheme)
            assertThat(newScheme.arrayDecorator)
                .isEqualTo(stringScheme.arrayDecorator)
                .isNotSameAs(stringScheme.arrayDecorator)
        }
    }
})
