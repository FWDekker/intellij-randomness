package com.fwdekker.randomness.fixedlength

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.fixedlength.FixedLengthDecorator.Companion.MIN_LENGTH
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [FixedLengthDecorator].
 */
object FixedLengthDecoratorTest : Spek({
    lateinit var fixedLengthDecorator: FixedLengthDecorator
    lateinit var dummyScheme: DummyScheme


    beforeEachTest {
        fixedLengthDecorator = FixedLengthDecorator(enabled = true)
        dummyScheme = DummyScheme(decorators = listOf(fixedLengthDecorator))
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            fixedLengthDecorator.length = -14

            assertThatThrownBy { dummyScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns variable-length values if disabled") {
            fixedLengthDecorator.enabled = false
            fixedLengthDecorator.length = 5
            dummyScheme.literals = listOf("property")

            assertThat(dummyScheme.generateStrings()).containsExactly("property")
        }

        it("shortens the given string if it is too long") {
            fixedLengthDecorator.length = 3
            dummyScheme.literals = listOf("recent")

            assertThat(dummyScheme.generateStrings()).containsExactly("rec")
        }

        it("pads the given string if it is too short") {
            fixedLengthDecorator.length = 6
            fixedLengthDecorator.filler = "o"
            dummyScheme.literals = listOf("c")

            assertThat(dummyScheme.generateStrings()).containsExactly("oooooc")
        }

        it("does not change the string if it already has the desired length") {
            fixedLengthDecorator.length = 9
            fixedLengthDecorator.filler = "o"
            dummyScheme.literals = listOf("permanent")

            assertThat(dummyScheme.generateStrings()).containsExactly("permanent")
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(FixedLengthDecorator().doValidate()).isNull()
        }

        it("fails if the length is too low") {
            fixedLengthDecorator.length = -659

            assertThat(fixedLengthDecorator.doValidate()).isEqualTo("Fixed length should be at least $MIN_LENGTH.")
        }

        it("fails if the filler is an empty string") {
            fixedLengthDecorator.filler = ""

            assertThat(fixedLengthDecorator.doValidate()).isEqualTo("Filler should be exactly one character.")
        }

        it("fails if the filler is a multi-character string") {
            fixedLengthDecorator.filler = "3lq6u"

            assertThat(fixedLengthDecorator.doValidate()).isEqualTo("Filler should be exactly one character.")
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            fixedLengthDecorator.length = 7

            val copy = fixedLengthDecorator.deepCopy()
            copy.length = 458

            assertThat(fixedLengthDecorator.length).isEqualTo(7)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            fixedLengthDecorator.enabled = false
            fixedLengthDecorator.length = 837
            fixedLengthDecorator.filler = "n"

            val newScheme = FixedLengthDecorator()
            newScheme.copyFrom(fixedLengthDecorator)

            assertThat(newScheme).isEqualTo(fixedLengthDecorator)
            assertThat(newScheme).isNotSameAs(fixedLengthDecorator)
        }
    }
})
