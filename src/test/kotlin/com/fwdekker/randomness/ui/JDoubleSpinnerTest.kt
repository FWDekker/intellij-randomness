package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Unit tests for [JDoubleSpinner].
 */
object JDoubleSpinnerTest : Spek({
    describe("constructor failures") {
        it("should fail on an illegal minimum value") {
            assertThatThrownBy { JDoubleSpinner(-1E80, -477.23) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("minValue should not be smaller than -1.0E53.")
        }

        it("should fail on an illegal maximum value") {
            assertThatThrownBy { JDoubleSpinner(-161.29, 1E73) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("maxValue should not be greater than 1.0E53.")
        }

        it("should fail on an illegal range") {
            assertThatThrownBy { JDoubleSpinner(-602.98, -929.41) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("minValue should be greater than maxValue.")
        }
    }

    describe("adjusting the value") {
        it("should get and set the value") {
            val spinner = JDoubleSpinner()

            spinner.value = 179.40

            assertThat(spinner.value).isEqualTo(179.40)
        }

        it("should return a double even if a long is set") {
            val spinner = JDoubleSpinner()

            spinner.setValue(638L)

            assertThat(spinner.value).isEqualTo(638.0)
        }
    }

    describe("validation") {
        it("should fail if the value is too low") {
            val spinner = JDoubleSpinner()

            spinner.value = -1E55

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -1.0E53.")
        }

        it("should fail if the value is too large") {
            val spinner = JDoubleSpinner()

            spinner.value = 1E98

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value less than or equal to 1.0E53.")
        }

        it("should fail if the value is below the set range") {
            val spinner = JDoubleSpinner(-738.33, 719.45)

            spinner.value = -808.68

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value greater than or equal to -738.33.")
        }

        it("should fail if the value is above the set range") {
            val spinner = JDoubleSpinner(-972.80, -69.36)

            spinner.value = 94.0

            val info = spinner.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("Please enter a value less than or equal to -69.36.")
        }
    }
})
