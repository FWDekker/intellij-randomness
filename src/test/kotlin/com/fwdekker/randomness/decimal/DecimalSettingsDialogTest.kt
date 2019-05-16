package com.fwdekker.randomness.decimal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * GUI tests for [DecimalSettingsDialog].
 */
object DecimalSettingsDialogTest : Spek({
    lateinit var decimalSettings: DecimalSettings
    lateinit var decimalSettingsDialog: DecimalSettingsDialog
    lateinit var frame: FrameFixture


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        decimalSettings = DecimalSettings()
        decimalSettings.minValue = 157.61
        decimalSettings.maxValue = 408.68
        decimalSettings.decimalCount = 5
        decimalSettings.showTrailingZeroes = false
        decimalSettings.groupingSeparator = "_"
        decimalSettings.decimalSeparator = "."

        decimalSettingsDialog =
            GuiActionRunner.execute<DecimalSettingsDialog> { DecimalSettingsDialog(decimalSettings) }
        frame = showInFrame(decimalSettingsDialog.createCenterPanel())
    }

    afterEachTest {
        frame.cleanUp()
    }


    describe("loading settings") {
        it("loads the settings' minimum value") {
            frame.spinner("minValue").requireValue(157.61)
        }

        it("loads the settings' maximum value") {
            frame.spinner("maxValue").requireValue(408.68)
        }

        it("loads the settings' decimal count") {
            frame.spinner("decimalCount").requireValue(5)
        }

        it("loads the settings' value for showing trailing zeroes") {
            frame.checkBox("showTrailingZeroes").requireSelected(false)
        }

        it("loads the settings' grouping separator") {
            frame.radioButton("groupingSeparatorNone").requireSelected(false)
            frame.radioButton("groupingSeparatorPeriod").requireSelected(false)
            frame.radioButton("groupingSeparatorComma").requireSelected(false)
            frame.radioButton("groupingSeparatorUnderscore").requireSelected(true)
        }

        it("loads the settings' decimal separator") {
            frame.radioButton("decimalSeparatorComma").requireSelected(false)
            frame.radioButton("decimalSeparatorPeriod").requireSelected(true)
        }
    }

    describe("input handling") {
        it("truncates decimals in the decimal count") {
            GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 693.57f }

            frame.spinner("decimalCount").requireValue(693)
        }
    }

    describe("validation") {
        it("passes for the default settings") {
            GuiActionRunner.execute { decimalSettingsDialog.loadSettings(DecimalSettings()) }

            assertThat(decimalSettingsDialog.doValidate()).isNull()
        }

        describe("value range") {
            it("fails if the minimum value is greater than the maximum value") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = 46.16
                    frame.spinner("maxValue").target().value = 45.16
                }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The maximum should not be smaller than the minimum.")
            }

            it("fails if the range size overflows") {
                GuiActionRunner.execute {
                    frame.spinner("minValue").target().value = -1E53
                    frame.spinner("maxValue").target().value = 1E53
                }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("maxValue").target())
                assertThat(validationInfo?.message).isEqualTo("The range should not exceed 1.0E53.")
            }
        }

        describe("decimal count") {
            it("passes if the decimal count is zero") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = 0 }

                assertThat(decimalSettingsDialog.doValidate()).isNull()
            }

            it("fails if the decimal count is negative") {
                GuiActionRunner.execute { frame.spinner("decimalCount").target().value = -851 }

                val validationInfo = decimalSettingsDialog.doValidate()

                assertThat(validationInfo).isNotNull()
                assertThat(validationInfo?.component).isEqualTo(frame.spinner("decimalCount").target())
                assertThat(validationInfo?.message).isEqualTo("Please enter a value greater than or equal to 0.")
            }
        }
    }

    describe("saving settings") {
        it("correctly saves settings to a settings object") {
            GuiActionRunner.execute {
                frame.spinner("minValue").target().value = 112.54
                frame.spinner("maxValue").target().value = 644.74
                frame.spinner("decimalCount").target().value = 485
                frame.checkBox("showTrailingZeroes").target().isSelected = false
                frame.radioButton("groupingSeparatorUnderscore").target().isSelected = true
                frame.radioButton("decimalSeparatorComma").target().isSelected = true
            }

            decimalSettingsDialog.saveSettings()

            assertThat(decimalSettings.minValue).isEqualTo(112.54)
            assertThat(decimalSettings.maxValue).isEqualTo(644.74)
            assertThat(decimalSettings.decimalCount).isEqualTo(485)
            assertThat(decimalSettings.showTrailingZeroes).isEqualTo(false)
            assertThat(decimalSettings.groupingSeparator).isEqualTo("_")
            assertThat(decimalSettings.decimalSeparator).isEqualTo(",")
        }
    }
})
