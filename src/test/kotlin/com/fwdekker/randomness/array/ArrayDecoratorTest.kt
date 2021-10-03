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
object ArrayDecoratorTest : Spek({
    lateinit var arrayDecorator: ArrayDecorator
    lateinit var dummyScheme: DummyScheme


    beforeEachTest {
        arrayDecorator = ArrayDecorator(enabled = true)
        dummyScheme = DummyScheme(decorators = listOf(arrayDecorator))
    }


    describe("generateStrings") {
        it("throws an exception if the scheme is invalid") {
            arrayDecorator.count = -321

            assertThatThrownBy { dummyScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns non-array values if disabled") {
            arrayDecorator.enabled = false
            dummyScheme.literals = listOf("Heal")

            assertThat(dummyScheme.generateStrings()).containsExactly("Heal")
        }

        describe("brackets") {
            it("returns an array-like string given no brackets") {
                arrayDecorator.count = 4
                arrayDecorator.brackets = ""
                arrayDecorator.separator = "h"
                arrayDecorator.isSpaceAfterSeparator = false
                dummyScheme.literals = listOf("Elvish", "Stride")

                assertThat(dummyScheme.generateStrings()).containsExactly("ElvishhStridehElvishhStride")
            }

            it("returns an array-like string with the brackets on both sides of the output") {
                arrayDecorator.count = 2
                arrayDecorator.brackets = "yn"
                dummyScheme.literals = listOf("Fatten", "Across")

                assertThat(dummyScheme.generateStrings()).containsExactly("ynFatten, Acrossyn")
            }

            it("returns an array-like string with the different brackets surrounding the input") {
                arrayDecorator.count = 2
                arrayDecorator.brackets = "wl@ga"
                dummyScheme.literals = listOf("Cloud", "Taxi")

                assertThat(dummyScheme.generateStrings()).containsExactly("wlCloud, Taxiga")
            }
        }

        describe("separator") {
            it("returns an array-like string given a non-singular separator") {
                arrayDecorator.count = 3
                arrayDecorator.brackets = "[@#"
                arrayDecorator.separator = ";;"
                arrayDecorator.isSpaceAfterSeparator = true
                dummyScheme.literals = listOf("Garhwali", "Pattypan", "Troll")

                assertThat(dummyScheme.generateStrings()).containsExactly("[Garhwali;; Pattypan;; Troll#")
            }

            it("returns an array-like string given a disabled space-after-separator") {
                arrayDecorator.count = 3
                arrayDecorator.brackets = "<@>"
                arrayDecorator.separator = "-"
                arrayDecorator.isSpaceAfterSeparator = false
                dummyScheme.literals = listOf("Remain", "Pound")

                assertThat(dummyScheme.generateStrings()).containsExactly("<Remain-Pound-Remain>")
            }

            it("returns an array-like string without space after separator given the newline separator") {
                arrayDecorator.count = 2
                arrayDecorator.brackets = "[@]"
                arrayDecorator.separator = "\n"
                arrayDecorator.isSpaceAfterSeparator = true
                dummyScheme.literals = listOf("Union", "Bell")

                assertThat(dummyScheme.generateStrings()).containsExactly("[Union\nBell]")
            }
        }

        it("returns multiple array-like strings that appropriately chunk the underlying generator's outputs") {
            arrayDecorator.count = 2
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
                arrayDecorator.count = 368

                assertThat(arrayDecorator.doValidate()).isNull()
            }

            it("fails for count equals 0") {
                arrayDecorator.count = 0

                assertThat(arrayDecorator.doValidate()).isEqualTo("Minimum count should be at least 1.")
            }

            it("fails for negative count") {
                arrayDecorator.count = -23

                assertThat(arrayDecorator.doValidate()).isEqualTo("Minimum count should be at least 1.")
            }
        }
    }

    describe("deepCopy") {
        it("creates an independent copy") {
            arrayDecorator.count = 44

            val copy = arrayDecorator.deepCopy()
            copy.count = 15

            assertThat(arrayDecorator.count).isEqualTo(44)
        }
    }

    describe("copyFrom") {
        it("copies state from another instance") {
            arrayDecorator.enabled = false
            arrayDecorator.count = 997
            arrayDecorator.brackets = "0fWx<@i6jTJ"
            arrayDecorator.customBrackets = "Wvtx2Lz7"
            arrayDecorator.separator = "f3hu)Rxiz1"
            arrayDecorator.customSeparator = "pKlq0b2"
            arrayDecorator.isSpaceAfterSeparator = false

            val newScheme = ArrayDecorator()
            newScheme.copyFrom(arrayDecorator)

            assertThat(newScheme).isEqualTo(arrayDecorator)
            assertThat(newScheme).isNotSameAs(arrayDecorator)
        }
    }
})
