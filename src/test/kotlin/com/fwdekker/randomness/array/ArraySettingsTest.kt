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


    describe("state management") {
        it("creates an independent copy") {
            val copy = arraySettings.deepCopy()
            arraySettings.count = 44
            copy.count = 15

            assertThat(arraySettings.count).isEqualTo(44)
        }

        it("copies state from another instance") {
            arraySettings.count = 997
            arraySettings.brackets = "0fWx<i6jTJ"
            arraySettings.separator = "f3hu)Rxiz1"
            arraySettings.isSpaceAfterSeparator = false

            val newArraySettings = ArraySettings()
            newArraySettings.loadState(arraySettings.state)

            assertThat(newArraySettings.count).isEqualTo(997)
            assertThat(newArraySettings.brackets).isEqualTo("0fWx<i6jTJ")
            assertThat(newArraySettings.separator).isEqualTo("f3hu)Rxiz1")
            assertThat(newArraySettings.isSpaceAfterSeparator).isEqualTo(false)
        }
    }

    describe("arrayify") {
        it("returns only the brackets if the array is empty") {
            assertThat(arraySettings.arrayify(emptyList())).isEqualTo("[]")
        }

        it("arrayifies an array") {
            arraySettings.count = 4
            arraySettings.brackets = "@#"
            arraySettings.separator = ";;"
            arraySettings.isSpaceAfterSeparator = true

            assertThat(arraySettings.arrayify(listOf("Garhwali", "Pattypan", "Troll")))
                .isEqualTo("@Garhwali;; Pattypan;; Troll#")
        }

        it("arrayifies an array without brackets") {
            arraySettings.count = 8
            arraySettings.brackets = ""
            arraySettings.separator = "h"
            arraySettings.isSpaceAfterSeparator = false

            assertThat(arraySettings.arrayify(listOf("Elvish", "Stride", "Bills"))).isEqualTo("ElvishhStridehBills")
        }

        it("does not place a space after separator if that option is false") {
            arraySettings.count = 3
            arraySettings.brackets = "<>"
            arraySettings.separator = "-"
            arraySettings.isSpaceAfterSeparator = false

            assertThat(arraySettings.arrayify(listOf("Remain", "Pound"))).isEqualTo("<Remain-Pound>")
        }

        it("ignores space after separator if newline separator is used") {
            arraySettings.count = 2
            arraySettings.brackets = "[]"
            arraySettings.separator = "\n"
            arraySettings.isSpaceAfterSeparator = true

            assertThat(arraySettings.arrayify(listOf("Union", "Bell"))).isEqualTo("[Union\nBell]")
        }
    }
})
