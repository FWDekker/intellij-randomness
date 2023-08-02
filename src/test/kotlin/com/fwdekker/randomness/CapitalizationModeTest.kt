package com.fwdekker.randomness

import io.kotest.assertions.retry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes


/**
 * Unit tests for [CapitalizationMode].
 */
object CapitalizationModeTest : FunSpec({
    val random = Random.Default


    test("transform") {
        test("retain mode") {
            test("does nothing to a string") {
                CapitalizationMode.RETAIN.transform("LOREm IpSuM", random) shouldBe "LOREm IpSuM"
            }
        }

        test("sentence mode") {
            test("does nothing to an empty string") {
                CapitalizationMode.SENTENCE.transform("", random) shouldBe ""
            }

            test("changes a string to sentence case") {
                CapitalizationMode.SENTENCE.transform("LOrEM ipsUm", random) shouldBe "Lorem ipsum"
            }
        }

        test("uppercase mode") {
            test("changes all characters to uppercase") {
                CapitalizationMode.UPPER.transform("LoREm iPSUM", random) shouldBe "LOREM IPSUM"
            }
        }

        test("lowercase mode") {
            test("changes all characters to lowercase") {
                CapitalizationMode.LOWER.transform("lorEm IpSUM", random) shouldBe "lorem ipsum"
            }
        }

        test("first letter mode") {
            test("changes all first letters to uppercase") {
                CapitalizationMode.FIRST_LETTER.transform("lOReM IPSum", random) shouldBe "Lorem Ipsum"
            }
        }

        test("random mode") {
            test("changes the capitalization to something else") {
                retry(100, 1.minutes) {
                    val input = "lOReM ipsUm"
                    val output = CapitalizationMode.RANDOM.transform(input, random)

                    output shouldNotBe input // Has a chance of 0.002% of failing
                    output shouldBeEqualIgnoringCase input
                }
            }

            test("produces reproducibly random strings") {
                val transform = { CapitalizationMode.RANDOM.transform("lorem ipsum", Random(0)) }

                transform() shouldBe transform()
            }
        }
    }

    test("toLocalizedString") {
        test("returns the associated localized string") {
            CapitalizationMode.FIRST_LETTER.toLocalizedString() shouldBe Bundle("shared.capitalization.first_letter")
        }
    }
})
