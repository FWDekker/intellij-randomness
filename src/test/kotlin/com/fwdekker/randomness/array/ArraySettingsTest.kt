package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


/**
 * Unit tests for [ArraySchemeDecorator].
 */
object ArraySettingsTest : Spek({
    lateinit var arraySchemeDecorator: ArraySchemeDecorator


    beforeEachTest {
        arraySchemeDecorator = ArraySchemeDecorator()
    }


    describe("copying") {
        it("creates an independent copy") {
            val copy = arraySchemeDecorator.deepCopy()
            arraySchemeDecorator.currentScheme.count = 44
            copy.currentScheme.count = 15

            assertThat(arraySchemeDecorator.currentScheme.count).isEqualTo(44)
        }

        it("copies state from another instance") {
            arraySchemeDecorator.currentScheme.count = 997
            arraySchemeDecorator.currentScheme.brackets = "0fWx<i6jTJ"
            arraySchemeDecorator.currentScheme.separator = "f3hu)Rxiz1"
            arraySchemeDecorator.currentScheme.isSpaceAfterSeparator = false

            val newArraySchemeDecorator = ArraySchemeDecorator()
            newArraySchemeDecorator.copyFrom(arraySchemeDecorator.state)

            assertThat(newArraySchemeDecorator.currentScheme.count).isEqualTo(997)
            assertThat(newArraySchemeDecorator.currentScheme.brackets).isEqualTo("0fWx<i6jTJ")
            assertThat(newArraySchemeDecorator.currentScheme.separator).isEqualTo("f3hu)Rxiz1")
            assertThat(newArraySchemeDecorator.currentScheme.isSpaceAfterSeparator).isEqualTo(false)
        }
    }
})


/**
 * Unit tests for [ArraySchemeDecorator].
 */
object ArraySchemeTest : Spek({
    lateinit var arraySchemeDecorator: ArraySchemeDecorator


    beforeEachTest {
        arraySchemeDecorator = ArraySchemeDecorator()
    }


    describe("arrayify") {
        it("returns only the brackets if the array is empty") {
            assertThat(arraySchemeDecorator.arrayify(emptyList())).isEqualTo("[]")
        }

        it("arrayifies an array") {
            arraySchemeDecorator.count = 4
            arraySchemeDecorator.brackets = "@#"
            arraySchemeDecorator.separator = ";;"
            arraySchemeDecorator.isSpaceAfterSeparator = true

            assertThat(arraySchemeDecorator.arrayify(listOf("Garhwali", "Pattypan", "Troll")))
                .isEqualTo("@Garhwali;; Pattypan;; Troll#")
        }

        it("arrayifies an array without brackets") {
            arraySchemeDecorator.count = 8
            arraySchemeDecorator.brackets = ""
            arraySchemeDecorator.separator = "h"
            arraySchemeDecorator.isSpaceAfterSeparator = false

            assertThat(arraySchemeDecorator.arrayify(listOf("Elvish", "Stride", "Bills"))).isEqualTo("ElvishhStridehBills")
        }

        it("does not place a space after separator if that option is false") {
            arraySchemeDecorator.count = 3
            arraySchemeDecorator.brackets = "<>"
            arraySchemeDecorator.separator = "-"
            arraySchemeDecorator.isSpaceAfterSeparator = false

            assertThat(arraySchemeDecorator.arrayify(listOf("Remain", "Pound"))).isEqualTo("<Remain-Pound>")
        }

        it("ignores space after separator if newline separator is used") {
            arraySchemeDecorator.count = 2
            arraySchemeDecorator.brackets = "[]"
            arraySchemeDecorator.separator = "\n"
            arraySchemeDecorator.isSpaceAfterSeparator = true

            assertThat(arraySchemeDecorator.arrayify(listOf("Union", "Bell"))).isEqualTo("[Union\nBell]")
        }
    }

    describe("copying") {
        describe("copyFrom") {
            it("makes the two schemes equal") {
                val schemeA = ArraySchemeDecorator()
                val schemeB = ArraySchemeDecorator(myName = "Name")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeA.copyFrom(schemeB)

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }

        describe("copyAs") {
            it("makes two schemes equal except for the name") {
                val schemeA = ArraySchemeDecorator()
                val schemeB = schemeA.copyAs("NewName")
                assertThat(schemeA).isNotEqualTo(schemeB)

                schemeB.myName = schemeA.myName

                assertThat(schemeA).isEqualTo(schemeB)
            }
        }
    }
})
