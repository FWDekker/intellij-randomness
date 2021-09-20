package com.fwdekker.randomness

import com.fwdekker.randomness.Timely.GENERATOR_TIMEOUT
import com.fwdekker.randomness.Timely.generateTimely
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for the extension functions in `TimelyKt`.
 */
@Suppress("TooGenericExceptionThrown") // Acceptable for tests
object TimelyTest : Spek({
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
            assertThatThrownBy { generateTimely { throw Exception("An exception message.") } }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("An exception message.")
        }

        @Suppress("CastToNullableType") // No other option
        it("throws an exception even if the generator throws a message-less exception") {
            assertThatThrownBy { generateTimely { throw Exception(null as String?) } }
                .isInstanceOf(DataGenerationException::class.java)
                .hasMessage("java.lang.Exception")
        }
    }
})
