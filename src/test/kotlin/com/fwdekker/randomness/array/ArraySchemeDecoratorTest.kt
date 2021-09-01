package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [ArrayDecorator].
 */
object ArraySchemeDecoratorTest : Spek({
    lateinit var arrayDecorator: ArrayDecorator
    lateinit var dummyScheme: DummyScheme


    beforeEachTest {
        arrayDecorator = ArrayDecorator(enabled = true)
        dummyScheme = DummyScheme(decorators = listOf(arrayDecorator))
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            arrayDecorator.maxCount = -321

            assertThatThrownBy { dummyScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns non-array values if disabled") {
            arrayDecorator.enabled = false
            dummyScheme.literals = listOf("Heal")

            assertThat(dummyScheme.generateStrings()).containsExactly("Heal")
        }

        it("returns an array-like string given a non-singular separator") {
            arrayDecorator.minCount = 3
            arrayDecorator.maxCount = 3
            arrayDecorator.brackets = "@#"
            arrayDecorator.separator = ";;"
            arrayDecorator.isSpaceAfterSeparator = true
            dummyScheme.literals = listOf("Garhwali", "Pattypan", "Troll")

            assertThat(dummyScheme.generateStrings()).containsExactly("@Garhwali;; Pattypan;; Troll#")
        }

        it("returns an array-like string given no brackets") {
            arrayDecorator.minCount = 4
            arrayDecorator.maxCount = 4
            arrayDecorator.brackets = ""
            arrayDecorator.separator = "h"
            arrayDecorator.isSpaceAfterSeparator = false
            dummyScheme.literals = listOf("Elvish", "Stride")

            assertThat(dummyScheme.generateStrings()).containsExactly("ElvishhStridehElvishhStride")
        }

        it("returns an array-like string given a disabled separator") {
            arrayDecorator.minCount = 3
            arrayDecorator.maxCount = 3
            arrayDecorator.brackets = "<>"
            arrayDecorator.separator = "-"
            arrayDecorator.isSpaceAfterSeparator = false
            dummyScheme.literals = listOf("Remain", "Pound")

            assertThat(dummyScheme.generateStrings()).containsExactly("<Remain-Pound-Remain>")
        }

        it("returns an array-like string without space after separator given the newline separator") {
            arrayDecorator.minCount = 2
            arrayDecorator.maxCount = 2
            arrayDecorator.brackets = "[]"
            arrayDecorator.separator = "\n"
            arrayDecorator.isSpaceAfterSeparator = true
            dummyScheme.literals = listOf("Union", "Bell")

            assertThat(dummyScheme.generateStrings()).containsExactly("[Union\nBell]")
        }

        it("returns multiple array-like strings that appropriately chunk the underlying generator's outputs") {
            arrayDecorator.minCount = 2
            arrayDecorator.maxCount = 2
            dummyScheme.literals = listOf("Flesh", "Strap", "Stem")

            assertThat(dummyScheme.generateStrings(3)).containsExactly(
                "[Flesh, Strap]",
                "[Stem, Flesh]",
                "[Strap, Stem]"
            )
        }
    }


    describe("doValidate") {
        it("passes for the default settings") {
            assertThat(ArrayDecorator().doValidate()).isNull()
        }

        describe("count") {
            it("passes if the minimum count equals the maximum count") {
                arrayDecorator.minCount = 368
                arrayDecorator.maxCount = 368

                assertThat(arrayDecorator.doValidate()).isNull()
            }

            it("fails for count equals 0") {
                arrayDecorator.minCount = 0
                arrayDecorator.maxCount = 0

                assertThat(arrayDecorator.doValidate()).isEqualTo("Minimum count should be at least 1, but is 0.")
            }

            it("fails for negative count") {
                arrayDecorator.minCount = -23
                arrayDecorator.maxCount = -23

                assertThat(arrayDecorator.doValidate())
                    .isEqualTo("Minimum count should be at least 1, but is -23.")
            }

            it("fails if the minimum count is greater than the maximum count") {
                arrayDecorator.minCount = 522
                arrayDecorator.maxCount = 162

                assertThat(arrayDecorator.doValidate())
                    .isEqualTo("Minimum count should be greater than maximum count.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            arrayDecorator.minCount = 44

            val copy = arrayDecorator.deepCopy()
            copy.minCount = 15

            assertThat(arrayDecorator.minCount).isEqualTo(44)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            arrayDecorator.enabled = false
            arrayDecorator.minCount = 808
            arrayDecorator.maxCount = 997
            arrayDecorator.brackets = "0fWx<i6jTJ"
            arrayDecorator.separator = "f3hu)Rxiz1"
            arrayDecorator.isSpaceAfterSeparator = false

            val newScheme = ArrayDecorator()
            newScheme.copyFrom(arrayDecorator)

            assertThat(newScheme).isEqualTo(arrayDecorator)
            assertThat(newScheme).isNotSameAs(arrayDecorator)
        }
    }
})
