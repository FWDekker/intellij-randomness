package com.fwdekker.randomness.ui

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import javax.swing.JSpinner


/**
 * Unit tests for the extension functions in `JSpinnerHelperKt`.
 */
object JSpinnerRangeTest : DescribeSpec({
    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }


    describe("constructor") {
        it("throws an exception if the range is negative") {
            assertThatThrownBy { bindSpinners(createJSpinner(), createJSpinner(), -37.20) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("maxRange must be a positive number.")
        }
    }

    describe("automatic range correction") {
        it("updates the minimum spinner if the maximum goes below its value") {
            val min = createJSpinner(150.38)
            val max = createJSpinner(244.54)
            bindSpinners(min, max)

            GuiActionRunner.execute { max.value = -284.85 }

            assertThat(min.value).isEqualTo(-284.85)
        }

        it("updates the maximum spinner if the minimum goes above its value") {
            val min = createJSpinner(-656.88)
            val max = createJSpinner(105.41)
            bindSpinners(min, max)

            GuiActionRunner.execute { min.value = 684.41 }

            assertThat(max.value).isEqualTo(684.41)
        }
    }
})


private fun createJSpinner() =
    GuiActionRunner.execute<JSpinner> { JSpinner() }

private fun createJSpinner(value: Double) =
    GuiActionRunner.execute<JSpinner> { JSpinner().also { it.value = value } }
