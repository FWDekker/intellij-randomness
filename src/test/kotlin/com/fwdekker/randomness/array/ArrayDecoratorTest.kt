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
            arrayDecorator.minCount = -321

            assertThatThrownBy { dummyScheme.generateStrings() }.isInstanceOf(DataGenerationException::class.java)
        }

        it("returns non-array values if disabled") {
            arrayDecorator.enabled = false
            dummyScheme.literals = listOf("Heal")

            assertThat(dummyScheme.generateStrings()).containsExactly("Heal")
        }

        describe("count") {
            it("generates a single value") {
                arrayDecorator.minCount = 1
                arrayDecorator.maxCount = 1
                arrayDecorator.separator = ","

                dummyScheme.generateStrings(50).forEach { string ->
                    assertThat(string.count { it == ',' }).isZero()
                }
            }

            it("generates a fixed number of values") {
                arrayDecorator.minCount = 8
                arrayDecorator.maxCount = 8
                arrayDecorator.separator = ","

                dummyScheme.generateStrings(50).forEach { string ->
                    assertThat(string.count { it == ',' }).isEqualTo(7)
                }
            }

            it("generates at least the minimum number of values") {
                arrayDecorator.minCount = 4
                arrayDecorator.maxCount = 12
                arrayDecorator.separator = ","

                dummyScheme.generateStrings(50).forEach { string ->
                    assertThat(string.count { it == ',' }).isGreaterThanOrEqualTo(3)
                }
            }

            it("generates at most the maximum number of values") {
                arrayDecorator.minCount = 9
                arrayDecorator.maxCount = 82
                arrayDecorator.separator = ","

                dummyScheme.generateStrings(50).forEach { string ->
                    assertThat(string.count { it == ',' }).isLessThanOrEqualTo(81)
                }
            }
        }

        describe("brackets") {
            it("returns an array-like string given no brackets") {
                arrayDecorator.minCount = 4
                arrayDecorator.maxCount = 4
                arrayDecorator.brackets = ""
                arrayDecorator.separator = "h"
                arrayDecorator.isSpaceAfterSeparator = false
                dummyScheme.literals = listOf("Elvish", "Stride")

                assertThat(dummyScheme.generateStrings()).containsExactly("ElvishhStridehElvishhStride")
            }

            it("puts brackets on both sides of the output if there is no @ in the brackets") {
                arrayDecorator.minCount = 2
                arrayDecorator.maxCount = 2
                arrayDecorator.brackets = "yn"
                dummyScheme.literals = listOf("Fatten", "Across")

                assertThat(dummyScheme.generateStrings()).containsExactly("ynFatten, Acrossyn")
            }

            it("substitutes the @ in the brackets with the output") {
                arrayDecorator.minCount = 2
                arrayDecorator.maxCount = 2
                arrayDecorator.brackets = "(@)"
                dummyScheme.literals = listOf("Cloud", "Taxi")

                assertThat(dummyScheme.generateStrings()).containsExactly("(Cloud, Taxi)")
            }
        }

        describe("separator") {
            it("returns an array-like string given a non-singular separator") {
                arrayDecorator.minCount = 3
                arrayDecorator.brackets = "[@#"
                arrayDecorator.separator = ";;"
                arrayDecorator.isSpaceAfterSeparator = true
                dummyScheme.literals = listOf("Garhwali", "Pattypan", "Troll")

                assertThat(dummyScheme.generateStrings()).containsExactly("[Garhwali;; Pattypan;; Troll#")
            }

            it("returns an array-like string given a disabled space-after-separator") {
                arrayDecorator.minCount = 3
                arrayDecorator.maxCount = 3
                arrayDecorator.brackets = "<@>"
                arrayDecorator.separator = "-"
                arrayDecorator.isSpaceAfterSeparator = false
                dummyScheme.literals = listOf("Remain", "Pound")

                assertThat(dummyScheme.generateStrings()).containsExactly("<Remain-Pound-Remain>")
            }

            it("returns an array-like string without space after separator given the newline separator") {
                arrayDecorator.minCount = 2
                arrayDecorator.maxCount = 2
                arrayDecorator.brackets = "[@]"
                arrayDecorator.separator = "\n"
                arrayDecorator.isSpaceAfterSeparator = true
                dummyScheme.literals = listOf("Union", "Bell")

                assertThat(dummyScheme.generateStrings()).containsExactly("[Union\nBell]")
            }
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
            it("fails for min count equals 0") {
                arrayDecorator.minCount = 0

                assertThat(arrayDecorator.doValidate()).isEqualTo("Minimum count should be at least 1.")
            }

            it("fails for negative min count") {
                arrayDecorator.minCount = -23

                assertThat(arrayDecorator.doValidate()).isEqualTo("Minimum count should be at least 1.")
            }

            it("passes if the minimum count equals the maximum count") {
                arrayDecorator.minCount = 368
                arrayDecorator.maxCount = 368

                assertThat(arrayDecorator.doValidate()).isNull()
            }

            it("fails if max count is less than min count") {
                arrayDecorator.minCount = 14
                arrayDecorator.maxCount = 2

                assertThat(arrayDecorator.doValidate())
                    .isEqualTo("Minimum count should be less than or equal to maximum count.")
            }
        }

        describe("brackets") {
            it("passes for valid brackets") {
                arrayDecorator.brackets = "dVN(An@)\\yk"

                assertThat(arrayDecorator.doValidate()).isNull()
            }

            it("fails for invalid brackets") {
                arrayDecorator.brackets = "zFT<pgaQH@\\"

                assertThat(arrayDecorator.doValidate()).isNotNull()
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
            arrayDecorator.minCount = 34
            arrayDecorator.maxCount = 830
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
