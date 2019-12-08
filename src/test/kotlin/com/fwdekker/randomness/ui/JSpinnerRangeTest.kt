package com.fwdekker.randomness.ui

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import javax.swing.JSpinner


/**
 * Unit tests for [JSpinnerRange].
 */
object JSpinnerRangeTest : Spek({
    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }


    describe("constructor") {
        it("throws an exception if the range is negative") {
            assertThatThrownBy { JSpinnerRange(createJSpinner(), createJSpinner(), -37.20) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("maxRange must be a positive number.")
        }
    }

    describe("validation") {
        it("returns null if the range is valid") {
            val range = JSpinnerRange(createJSpinner(287.01), createJSpinner(448.50), 758.34)

            assertThat(range.validateValue()).isNull()
        }

        it("returns validation information if the minimum is greater than the maximum") {
            val range = JSpinnerRange(createJSpinner(85.20), createJSpinner(-636.33))

            val info = range.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("The maximum should not be smaller than the minimum.")
        }

        it("returns validation information if the default range is exceeded") {
            val range = JSpinnerRange(createJSpinner(-1E53), createJSpinner(1E53))

            val info = range.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("The range should not exceed 1.0E53.")
        }

        it("returns validation information if a custom range is exceeded") {
            val range = JSpinnerRange(createJSpinner(-794.90), createJSpinner(759.52), 793.31)

            val info = range.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("The range should not exceed 793.31.")
        }

        it("returns validation information with the custom range name if the range is exceeded") {
            val range = JSpinnerRange(createJSpinner(459.18), createJSpinner(214.93), name = "name")

            val info = range.validateValue()
            assertThat(info).isNotNull()
            assertThat(info?.message).isEqualTo("The maximum name should not be smaller than the minimum name.")
        }
    }

    describe("automatic range correction") {
        it("updates the minimum spinner if the maximum goes below its value") {
            val min = createJSpinner(150.38)
            val max = createJSpinner(244.54)
            JSpinnerRange(min, max)

            GuiActionRunner.execute { max.value = -284.85 }

            assertThat(min.value).isEqualTo(-284.85)
        }

        it("updates the maximum spinner if the minimum goes above its value") {
            val min = createJSpinner(-656.88)
            val max = createJSpinner(105.41)
            JSpinnerRange(min, max)

            GuiActionRunner.execute { min.value = 684.41 }

            assertThat(max.value).isEqualTo(684.41)
        }
    }
})


private fun createJSpinner() =
    GuiActionRunner.execute<JSpinner> { JSpinner() }

private fun createJSpinner(value: Double) =
    GuiActionRunner.execute<JSpinner> { JSpinner().also { it.value = value } }
