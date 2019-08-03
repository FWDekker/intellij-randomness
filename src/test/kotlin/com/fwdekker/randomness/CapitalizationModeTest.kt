package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.NoSuchElementException


/**
 * Unit tests for [CapitalizationMode].
 */
object CapitalizationModeTest : Spek({
    describe("transform") {
        describe("retain mode") {
            it("does nothing to a string") {
                assertThat(CapitalizationMode.RETAIN.transform.invoke("AwfJYzzUoR")).isEqualTo("AwfJYzzUoR")
            }
        }

        describe("sentence mode") {
            it("does nothing to an empty string") {
                assertThat(CapitalizationMode.SENTENCE.transform.invoke("")).isEqualTo("")
            }

            it("changes a string to sentence case") {
                assertThat(CapitalizationMode.SENTENCE.transform.invoke("cOoKiE cAN")).isEqualTo("Cookie can")
            }
        }

        describe("uppercase mode") {
            it("changes all characters to uppercase") {
                assertThat(CapitalizationMode.UPPER.transform.invoke("vAnDaLisM")).isEqualTo("VANDALISM")
            }
        }

        describe("lowercase mode") {
            it("changes all characters to lowercase") {
                assertThat(CapitalizationMode.LOWER.transform.invoke("ChAnnEl")).isEqualTo("channel")
            }
        }

        describe("first letter mode") {
            it("changes all first letters to uppercase") {
                assertThat(CapitalizationMode.FIRST_LETTER.transform.invoke("bgiOP SMQpR")).isEqualTo("Bgiop Smqpr")
            }
        }

        describe("random mode") {
            it("changes the capitalization to something else") {
                assertThat(CapitalizationMode.RANDOM.transform.invoke("GHmdukhNqua"))
                    .isNotEqualTo("GHmdukhNqua") // Has a chance of 0.002% of failing
                    .isEqualToIgnoringCase("GHmdukhNqua")
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

    describe("finding mode by descriptor") {
        it("returns the capitalization mode based on its descriptor") {
            assertThat(CapitalizationMode.getMode("sentence")).isEqualTo(CapitalizationMode.SENTENCE)
        }

        it("throws an exception if the descriptor is not recognized") {
            assertThatThrownBy { CapitalizationMode.getMode("") }
                .isInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There does not exist a capitalization mode with name ``.")
                .hasNoCause()
        }
    }
})
