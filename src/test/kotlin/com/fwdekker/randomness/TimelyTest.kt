package com.fwdekker.randomness

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for the extension functions in `TimelyKt`.
 */
class TimelyTest : Spek({
    describe("timely") {
        it("returns output if generator finished within time") {
            assertThat(generateTimely { "neck" }).isEqualTo("neck")
        }

        it("throws an exception if the generator does not finish within time") {
            assertThatThrownBy {
                generateTimely<String> {
                    Thread.sleep(GENERATOR_TIMEOUT + 500L)
                    "may"
                }
            }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("Timed out while generating data.")
        }

        it("throws an exception if the generator throws an exception") {
            assertThatThrownBy { generateTimely { error("An exception message.") } }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("An exception message.")
        }
    }
})
