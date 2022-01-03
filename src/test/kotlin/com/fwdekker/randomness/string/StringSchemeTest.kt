package com.fwdekker.randomness.string

import com.fwdekker.randomness.Bundle
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

        it("returns a raw string if regex is disabled") {
            stringScheme.pattern = "a[bc]d"
            stringScheme.isRegex = false

            assertThat(stringScheme.generateStrings()).containsExactly("a[bc]d")
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

        it("fails if the pattern ends with a trailing backslash") {
            stringScheme.pattern = "14\\"

            assertThat(stringScheme.doValidate()).isEqualTo(Bundle("string.error.trailing_backslash"))
        }

        it("fails if the pattern ends with an odd number of trailing backslashes") {
            stringScheme.pattern = "deb\\\\\\"

            assertThat(stringScheme.doValidate()).isEqualTo(Bundle("string.error.trailing_backslash"))
        }

        it("does not fail if the trailing backslash is escaped") {
            stringScheme.pattern = "12\\\\"

            assertThat(stringScheme.doValidate()).isNull()
        }

        it("does not fail if the pattern has a trailing backslash but is not a regex") {
            stringScheme.pattern = "q\\"
            stringScheme.isRegex = false

            assertThat(stringScheme.doValidate()).isNull()
        }

        it("fails if the pattern is invalid") {
            stringScheme.pattern = "{9"

            assertThat(stringScheme.doValidate()).isEqualTo("Cannot repeat nothing at\n'{9'\n ^")
        }

        it("fails if the decorator is invalid") {
            stringScheme.arrayDecorator.minCount = -985

            assertThat(stringScheme.doValidate()).isNotNull()
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            stringScheme.pattern = "compose"
            stringScheme.isRegex = false
            stringScheme.arrayDecorator.minCount = 943

            val copy = stringScheme.deepCopy()
            copy.pattern = "tidy"
            copy.isRegex = true
            copy.arrayDecorator.minCount = 173

            assertThat(stringScheme.pattern).isEqualTo("compose")
            assertThat(stringScheme.isRegex).isEqualTo(false)
            assertThat(stringScheme.arrayDecorator.minCount).isEqualTo(943)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            stringScheme.pattern = "trust"
            stringScheme.isRegex = false
            stringScheme.capitalization = CapitalizationMode.RANDOM
            stringScheme.removeLookAlikeSymbols = true
            stringScheme.arrayDecorator.minCount = 249

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
