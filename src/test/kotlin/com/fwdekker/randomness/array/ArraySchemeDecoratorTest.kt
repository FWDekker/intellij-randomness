package com.fwdekker.randomness.array

import com.fwdekker.randomness.DataGenerationException
import com.fwdekker.randomness.DummyScheme
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [ArraySchemeDecorator].
 */
object ArraySchemeDecoratorTest : Spek({
    lateinit var arraySchemeDecorator: ArraySchemeDecorator
    lateinit var dummyScheme: DummyScheme


    beforeEachTest {
        arraySchemeDecorator = ArraySchemeDecorator(enabled = true)
        dummyScheme = DummyScheme(decorator = arraySchemeDecorator)
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            arraySchemeDecorator.count = -321

            assertThatThrownBy { dummyScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns non-array values if disabled") {
            arraySchemeDecorator.enabled = false
            dummyScheme.literals = listOf("Heal")

            assertThat(dummyScheme.generateStrings()).containsExactly("Heal")
        }

        it("returns an array-like string given a non-singular separator") {
            arraySchemeDecorator.count = 3
            arraySchemeDecorator.brackets = "@#"
            arraySchemeDecorator.separator = ";;"
            arraySchemeDecorator.isSpaceAfterSeparator = true
            dummyScheme.literals = listOf("Garhwali", "Pattypan", "Troll")

            assertThat(dummyScheme.generateStrings()).containsExactly("@Garhwali;; Pattypan;; Troll#")
        }

        it("returns an array-like string given no brackets") {
            arraySchemeDecorator.count = 4
            arraySchemeDecorator.brackets = ""
            arraySchemeDecorator.separator = "h"
            arraySchemeDecorator.isSpaceAfterSeparator = false
            dummyScheme.literals = listOf("Elvish", "Stride")

            assertThat(dummyScheme.generateStrings()).containsExactly("ElvishhStridehElvishhStride")
        }

        it("returns an array-like string given a disabled separator") {
            arraySchemeDecorator.count = 3
            arraySchemeDecorator.brackets = "<>"
            arraySchemeDecorator.separator = "-"
            arraySchemeDecorator.isSpaceAfterSeparator = false
            dummyScheme.literals = listOf("Remain", "Pound")

            assertThat(dummyScheme.generateStrings()).containsExactly("<Remain-Pound-Remain>")
        }

        it("returns an array-like string without space after separator given the newline separator") {
            arraySchemeDecorator.count = 2
            arraySchemeDecorator.brackets = "[]"
            arraySchemeDecorator.separator = "\n"
            arraySchemeDecorator.isSpaceAfterSeparator = true
            dummyScheme.literals = listOf("Union", "Bell")

            assertThat(dummyScheme.generateStrings()).containsExactly("[Union\nBell]")
        }

        it("returns multiple array-like strings that appropriately chunk the underlying generator's outputs") {
            arraySchemeDecorator.count = 2
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
            assertThat(ArraySchemeDecorator().doValidate()).isNull()
        }

        describe("count") {
            it("fails for count equals 0") {
                arraySchemeDecorator.count = 0

                assertThat(arraySchemeDecorator.doValidate()).isEqualTo("Minimum count should be at least 1, but is 0.")
            }

            it("fails for negative count") {
                arraySchemeDecorator.count = -23

                assertThat(arraySchemeDecorator.doValidate()).isNotNull()
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            arraySchemeDecorator.count = 44

            val copy = arraySchemeDecorator.deepCopy()
            copy.count = 15

            assertThat(arraySchemeDecorator.count).isEqualTo(44)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            arraySchemeDecorator.enabled = false
            arraySchemeDecorator.count = 997
            arraySchemeDecorator.brackets = "0fWx<i6jTJ"
            arraySchemeDecorator.separator = "f3hu)Rxiz1"
            arraySchemeDecorator.isSpaceAfterSeparator = false

            val newScheme = ArraySchemeDecorator()
            newScheme.copyFrom(arraySchemeDecorator)

            assertThat(newScheme).isEqualTo(arraySchemeDecorator)
            assertThat(newScheme).isNotSameAs(arraySchemeDecorator)
        }
    }
})
