package com.fwdekker.randomness

import com.fwdekker.randomness.Timely.GENERATOR_TIMEOUT
import com.fwdekker.randomness.Timely.generateTimely
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


/**
 * Unit tests for the extension functions in `TimelyKt`.
 */
object TimelyTest : FunSpec({
    test("returns output if generator finished within time") {
        generateTimely { "output" } shouldBe "output"
    }

    test("throws an exception if the generator does not finish within time") {
        shouldThrow<DataGenerationException> { generateTimely { Thread.sleep(GENERATOR_TIMEOUT + 1000L) } }
            .message shouldBe Bundle("helpers.error.timed_out")
    }

    test("throws an exception if the generator throws an exception") {
        shouldThrow<DataGenerationException> { generateTimely { throw UnsupportedOperationException("Error") } }
            .message shouldBe "Error"
    }

    test("throws an exception with the exception class if the exception has no message") {
        shouldThrow<DataGenerationException> { generateTimely { throw UnsupportedOperationException(null as String?) } }
            .message shouldBe "java.lang.UnsupportedOperationException"
    }
})
