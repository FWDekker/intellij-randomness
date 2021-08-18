package com.fwdekker.randomness.literal

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [LiteralScheme].
 */
object LiteralSchemeTest : Spek({
    lateinit var literalScheme: LiteralScheme


    beforeEachTest {
        literalScheme = LiteralScheme()
    }


    describe("generateStrings") {
        it("can generate an empty string") {
            literalScheme.literal = ""

            assertThat(literalScheme.generateStrings()).containsExactly("")
        }

        it("can generate a blank string") {
            literalScheme.literal = "  "

            assertThat(literalScheme.generateStrings()).containsExactly("  ")
        }

        it("can generate the given non-empty string") {
            literalScheme.literal = "shine"

            assertThat(literalScheme.generateStrings()).containsExactly("shine")
        }
    }


    describe("deepCopy") {
        it("creates an independent copy") {
            literalScheme.literal = "boast"
            literalScheme.decorator.count = 815

            val copy = literalScheme.deepCopy()
            copy.literal = "strict"
            copy.decorator.count = 844

            assertThat(literalScheme.literal).isEqualTo("boast")
            assertThat(literalScheme.decorator.count).isEqualTo(815)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            literalScheme.literal = "tame"
            literalScheme.decorator.count = 555

            val newScheme = LiteralScheme()
            newScheme.copyFrom(literalScheme)

            assertThat(newScheme)
                .isEqualTo(literalScheme)
                .isNotSameAs(literalScheme)
            assertThat(newScheme.decorator)
                .isEqualTo(literalScheme.decorator)
                .isNotSameAs(literalScheme.decorator)
        }
    }
})
