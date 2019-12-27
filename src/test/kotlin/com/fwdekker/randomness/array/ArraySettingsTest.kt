package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [ArraySettings].
 */
object ArraySettingsTest : Spek({
    lateinit var arraySettings: ArraySettings


    beforeEachTest {
        arraySettings = ArraySettings()
    }


    it("creates an independent copy") {
        val copy = arraySettings.deepCopy()
        arraySettings.currentScheme.count = 44
        copy.currentScheme.count = 15

        assertThat(arraySettings.currentScheme.count).isEqualTo(44)
    }

    it("copies state from another instance") {
        arraySettings.currentScheme.count = 997
        arraySettings.currentScheme.brackets = "0fWx<i6jTJ"
        arraySettings.currentScheme.separator = "f3hu)Rxiz1"
        arraySettings.currentScheme.isSpaceAfterSeparator = false

        val newArraySettings = ArraySettings()
        newArraySettings.loadState(arraySettings.state)

        assertThat(newArraySettings.currentScheme.count).isEqualTo(997)
        assertThat(newArraySettings.currentScheme.brackets).isEqualTo("0fWx<i6jTJ")
        assertThat(newArraySettings.currentScheme.separator).isEqualTo("f3hu)Rxiz1")
        assertThat(newArraySettings.currentScheme.isSpaceAfterSeparator).isEqualTo(false)
    }
})


/**
 * Unit tests for [ArrayScheme].
 */
object ArraySchemeTest : Spek({
    lateinit var arrayScheme: ArrayScheme


    beforeEachTest {
        arrayScheme = ArrayScheme()
    }


    describe("arrayify") {
        it("returns only the brackets if the array is empty") {
            assertThat(arrayScheme.arrayify(emptyList())).isEqualTo("[]")
        }

        it("arrayifies an array") {
            arrayScheme.count = 4
            arrayScheme.brackets = "@#"
            arrayScheme.separator = ";;"
            arrayScheme.isSpaceAfterSeparator = true

            assertThat(arrayScheme.arrayify(listOf("Garhwali", "Pattypan", "Troll")))
                .isEqualTo("@Garhwali;; Pattypan;; Troll#")
        }

        it("arrayifies an array without brackets") {
            arrayScheme.count = 8
            arrayScheme.brackets = ""
            arrayScheme.separator = "h"
            arrayScheme.isSpaceAfterSeparator = false

            assertThat(arrayScheme.arrayify(listOf("Elvish", "Stride", "Bills"))).isEqualTo("ElvishhStridehBills")
        }

        it("does not place a space after separator if that option is false") {
            arrayScheme.count = 3
            arrayScheme.brackets = "<>"
            arrayScheme.separator = "-"
            arrayScheme.isSpaceAfterSeparator = false

            assertThat(arrayScheme.arrayify(listOf("Remain", "Pound"))).isEqualTo("<Remain-Pound>")
        }

        it("ignores space after separator if newline separator is used") {
            arrayScheme.count = 2
            arrayScheme.brackets = "[]"
            arrayScheme.separator = "\n"
            arrayScheme.isSpaceAfterSeparator = true

            assertThat(arrayScheme.arrayify(listOf("Union", "Bell"))).isEqualTo("[Union\nBell]")
        }
    }
})
