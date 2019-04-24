package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [JLongSpinner].
 */
object JLongSpinnerTest : Spek({
    describe("constructor failures") {
        it("should fail if the maximum value is smaller than the minimum value") {
            assertThatThrownBy { JLongSpinner(414, 989, -339) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("(minimum <= value <= maximum) is false")
        }

        it("should fail if the value is below the set range") {
            assertThatThrownBy { JLongSpinner(34, 192, 251) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("(minimum <= value <= maximum) is false")
        }

        it("should fail if the value is above the set range") {
            assertThatThrownBy { JLongSpinner(194, 72, 125) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("(minimum <= value <= maximum) is false")
        }
    }

    describe("adjusting the value") {
        it("stores negative numbers") {
            val spinner = JLongSpinner()

            spinner.value = -583L

            assertThat(spinner.value).isEqualTo(-583L)
        }

        it("stores positive numbers") {
            val spinner = JLongSpinner()

            spinner.value = 125

            assertThat(spinner.value).isEqualTo(125L)
        }

        it("truncates when storing a double") {
            val spinner = JLongSpinner()

            spinner.setValue(786.79)

            assertThat(spinner.value).isEqualTo(786L)
        }
    }

    describe("adjusting the range") {
        it("changes the minimum value") {
            val spinner = JLongSpinner()

            spinner.minValue = 979L

            assertThat(spinner.minValue).isEqualTo(979L)
        }

        it("changes the maximum value") {
            val spinner = JLongSpinner()

            spinner.maxValue = 166L

            assertThat(spinner.maxValue).isEqualTo(166L)
        }
    }

    describe("validation") {
        it("should fail when the value is below the set range") {
            val spinner = JLongSpinner(-665, -950, -559)

            spinner.value = -979

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -950.")
        }

        it("should fail when the value is above the set range") {
            val spinner = JLongSpinner(424, 279, 678)

            spinner.value = 838

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value less than or equal to 678.")
        }
    }
})
