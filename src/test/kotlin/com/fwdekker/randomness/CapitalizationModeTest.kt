package com.fwdekker.randomness

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import kotlin.random.Random


/**
 * Unit tests for [CapitalizationMode].
 */
object CapitalizationModeTest : DescribeSpec({
    val random = Random.Default


    describe("transform") {
        describe("retain mode") {
            it("does nothing to a string") {
                assertThat(CapitalizationMode.RETAIN.transform("AwfJYzzUoR", random)).isEqualTo("AwfJYzzUoR")
            }
        }

        describe("sentence mode") {
            it("does nothing to an empty string") {
                assertThat(CapitalizationMode.SENTENCE.transform("", random)).isEqualTo("")
            }

            it("changes a string to sentence case") {
                assertThat(CapitalizationMode.SENTENCE.transform("cOoKiE cAN", random)).isEqualTo("Cookie can")
            }
        }

        describe("uppercase mode") {
            it("changes all characters to uppercase") {
                assertThat(CapitalizationMode.UPPER.transform("vAnDaLisM", random)).isEqualTo("VANDALISM")
            }
        }

        describe("lowercase mode") {
            it("changes all characters to lowercase") {
                assertThat(CapitalizationMode.LOWER.transform("ChAnnEl", random)).isEqualTo("channel")
            }
        }

        describe("first letter mode") {
            it("changes all first letters to uppercase") {
                assertThat(CapitalizationMode.FIRST_LETTER.transform("bgiOP SMQpR", random)).isEqualTo("Bgiop Smqpr")
            }
        }

        describe("random mode") {
            it("changes the capitalization to something else") {
                assertThat(CapitalizationMode.RANDOM.transform("GHmdukhNqua", random))
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
                assertThat(CapitalizationMode.DUMMY.transform("i4Oh51O", random)).isEqualTo("i4Oh51O")
            }
        }
    }
})
