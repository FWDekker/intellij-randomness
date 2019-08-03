package com.fwdekker.randomness.array

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


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
            val copy = arraySettings.copyState()
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
    }
})
