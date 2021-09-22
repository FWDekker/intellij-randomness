package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.random.Random


/**
 * Unit tests for [CapitalizationMode].
 */
object CapitalizationModeTest : Spek({
    describe("transform") {
        describe("retain mode") {
            it("does nothing to a string") {
                assertThat(CapitalizationMode.RETAIN.transform("AwfJYzzUoR")).isEqualTo("AwfJYzzUoR")
            }
        }

        describe("sentence mode") {
            it("does nothing to an empty string") {
                assertThat(CapitalizationMode.SENTENCE.transform("")).isEqualTo("")
            }

            it("changes a string to sentence case") {
                assertThat(CapitalizationMode.SENTENCE.transform("cOoKiE cAN")).isEqualTo("Cookie can")
            }
        }

        describe("uppercase mode") {
            it("changes all characters to uppercase") {
                assertThat(CapitalizationMode.UPPER.transform("vAnDaLisM")).isEqualTo("VANDALISM")
            }
        }

        describe("lowercase mode") {
            it("changes all characters to lowercase") {
                assertThat(CapitalizationMode.LOWER.transform("ChAnnEl")).isEqualTo("channel")
            }
        }

        describe("first letter mode") {
            it("changes all first letters to uppercase") {
                assertThat(CapitalizationMode.FIRST_LETTER.transform("bgiOP SMQpR")).isEqualTo("Bgiop Smqpr")
            }
        }

        describe("random mode") {
            it("changes the capitalization to something else") {
                assertThat(CapitalizationMode.RANDOM.transform("GHmdukhNqua"))
                    .isNotEqualTo("GHmdukhNqua") // Has a chance of 0.002% of failing
                    .isEqualToIgnoringCase("GHmdukhNqua")
            }

            it("produces reproducibly random strings") {
                val transform = { CapitalizationMode.RANDOM.transform("several", Random(15)) }

                assertThat(transform()).isEqualTo(transform())
            }
        }

        describe("dummy mode") {
            it("does nothing to a string") {
                assertThat(CapitalizationMode.DUMMY.transform("i4Oh51O")).isEqualTo("i4Oh51O")
            }
        }
    }

    describe("descriptor") {
        it("returns the name") {
            assertThat(CapitalizationMode.LOWER.descriptor).isEqualTo("lower")
        }
    }


    describe("toString") {
        it("returns the name in the toString method") {
            assertThat(CapitalizationMode.LOWER.toString()).isEqualTo("lower")
        }
    }


    describe("getMode") {
        it("returns the capitalization mode based on its descriptor") {
            assertThat(CapitalizationMode.getMode("sentence")).isEqualTo(CapitalizationMode.SENTENCE)
        }

        it("throws an exception if the descriptor is not recognized") {
            assertThatThrownBy { CapitalizationMode.getMode("river") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Capitalization mode 'river' does not exist.")
                .hasNoCause()
        }
    }
})
